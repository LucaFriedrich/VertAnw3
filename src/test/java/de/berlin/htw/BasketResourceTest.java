package de.berlin.htw;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;
import de.berlin.htw.boundary.dto.Item;

@QuarkusTest
class BasketResourceTest {

    @Inject
    protected RedisDataSource redisDS;
    private ValueCommands<String, Integer> countCommands;

    @BeforeEach
    void setUp() {
        countCommands = redisDS.value(Integer.class);
        // Setting up initial data in Redis
        countCommands.set("user:2:basket:item1:count", 88);
    }

    @Test
    void testGetBasket() {
        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .get("/basket")
                .then()
                .log().all()
                .statusCode(500);

        // Adjust the key to match the one used in your controller logic
        assertEquals(88, countCommands.get("user:2:basket:item1:count"));
    }

    @Test
    void testAddItem() {
        Item item = new Item();
        item.setProductName("Product");
        item.setProductId("1-2-3-4-5-6");
        item.setCount(1);
        item.setPrice(50f);

        given()
                .log().all()
                .header("X-User-Id", "3")
                .contentType(ContentType.JSON)
                .body(item)
                .when()
                .post("/basket/1-2-3-4-5-6")
                .then()
                .log().all()
                .statusCode(201);
    }

    @Test
    void testCheckout() {
        given()
                .log().all()
                .when().header("X-User-Id", "4")
                .post("/basket")
                .then()
                .log().all()
                .statusCode(201)
                .header("Location", "http://localhost:8081/orders");
    }
}