package de.berlin.htw.control;

import de.berlin.htw.boundary.dto.Basket;
import de.berlin.htw.boundary.dto.Item;
import de.berlin.htw.entity.dao.UserRepository;
import de.berlin.htw.entity.dto.UserEntity;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.list.ListCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Dependent
public class BasketController {

    @Inject
    UserRepository user;
    @Inject
    protected RedisDataSource redisDS;
    protected KeyCommands<String> keyCommands;
    protected ValueCommands<String, Integer> countCommands;
    protected ListCommands<String, String> stringListCommands;
    protected HashCommands<String, String, String> stringHashCommands;

    @Inject
    OrderController orderController;

    @PostConstruct
    protected void init() {
        countCommands = redisDS.value(Integer.class);
        stringListCommands = redisDS.list(String.class);
        stringHashCommands = redisDS.hash(String.class);
        keyCommands = redisDS.key();
    }

    public Basket getBasket(String userId) {
        String basketKey = "user:" + userId + ":basket";
        List<String> itemKeys = keyCommands.keys(basketKey + ":*");
        UserEntity userEntity = user.findUserById(Integer.valueOf(userId));
        List<Item> items = new ArrayList<>();
        Basket basket = new Basket();
        float userBalance = userEntity.getBalance();

        for (String key : itemKeys) {
            Map<String, String> data = stringHashCommands.hgetall(key);
            if (!data.isEmpty()) {
                Item item = new Item();
                item.setProductName(data.get("productName"));
                item.setProductId(data.get("productId"));
                item.setCount(Integer.parseInt(data.get("count")));
                item.setPrice(Float.parseFloat(data.get("price")));
                items.add(item);
                userBalance = userBalance - item.getPrice() * item.getCount();
            }
        }
        basket.setItems(items);
        basket.setRemainingBalance(userBalance);
        basket.setTotal(userEntity.getBalance() - userBalance);
        return basket;
    }

    public Basket addItem(String userId, String productId, @Valid Item item) {
        Basket basket = getBasket(userId);
        if (basket == null) {
            basket = new Basket();
        }
        if (basket.getItems().size() >= 10) {
            throw new RuntimeException("Cannot add more than 10 items to the basket");
        }
        float basketBalance = basket.getRemainingBalance();
        String basketUserKey = "user:" + userId + ":basket:" + item.getProductId();
        stringHashCommands.hset(basketUserKey, "productName", item.getProductName());
        stringHashCommands.hset(basketUserKey, "productId", item.getProductId());
        stringHashCommands.hset(basketUserKey, "count", String.valueOf(item.getCount()));
        stringHashCommands.hset(basketUserKey, "price", String.valueOf(item.getPrice()));

        basketBalance = basketBalance - item.getPrice() * item.getCount();
        basket.setRemainingBalance(basketBalance);
        if (basket.getTotal() == null) basket.setTotal(item.getPrice() * item.getCount());
        else basket.setTotal(basket.getTotal() + item.getPrice() * item.getCount());
        refreshExpiry(userId);
        return basket;
    }

    public Basket removeItem(String userId, String productId) {
        String basketUserKey = "user:" + userId + ":basket:" + productId;
        keyCommands.del(basketUserKey);
        refreshExpiry(userId);
        return getBasket(userId);
    }

    public Basket changeCount(String userId, @Valid Item item) {
        String basketUserKey = "user:" + userId + ":basket:" + item.getProductId();
        stringHashCommands.hincrby(basketUserKey, "count", item.getCount());
        refreshExpiry(userId);
        return getBasket(userId);
    }

    public void clearBasket(String userId) {
        String keys = "user:" + userId + ":basket:*";
        List<String> itemKeys = keyCommands.keys(keys);
        for (String key : itemKeys) {
            keyCommands.del(key);
        }
    }

    private void refreshExpiry(String userId) {
        String keys = "user:" + userId + ":basket:*";
        List<String> itemKeys = keyCommands.keys(keys);
        for (String key : itemKeys) {
            keyCommands.expire(key, 120); // 2 minutes
        }
    }

    @Transactional
    public void checkout(String userId) {
        Basket basket = getBasket(userId);
        if (basket == null) throw new RuntimeException("Basket is empty!");

        if (basket.getTotal() != null) {
            UserEntity userEntity = user.findUserById(Integer.valueOf(userId));
            userEntity.setBalance(userEntity.getBalance() - basket.getTotal());
            user.persistUser(userEntity);
        }

        clearBasket(userId);
    }
}
