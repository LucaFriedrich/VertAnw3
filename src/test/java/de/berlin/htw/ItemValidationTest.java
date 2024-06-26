package de.berlin.htw;

import de.berlin.htw.boundary.dto.Item;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class ItemValidationTest {

    @Test
    void testInvalidProductName() {
        Item item = new Item();
        item.setProductName("a".repeat(300)); // Artikelname ist zu lang
        item.setProductId("1-2-3-4-5-6");
        item.setCount(1);
        item.setPrice(50f);

        given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "3")
                .body(item)
                .when()
                .post("/basket/1-2-3-4-5-6")
                .then()
                .statusCode(400)
                .body("parameterViolations.message[0]", containsString("Artikelname darf nicht länger als 255 Zeichen sein"));
    }

    @Test
    void testInvalidPrice() {
        Item item = new Item();
        item.setProductName("Product");
        item.setProductId("1-2-3-4-5-6");
        item.setCount(1);
        item.setPrice(200f); // Preis ist zu hoch

        given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "3")
                .body(item)
                .when()
                .post("/basket/1-2-3-4-5-6")
                .then()
                .statusCode(400)
                .body("parameterViolations.message[0]", containsString("Preis darf höchstens 100 Euro sein"));
    }

    @Test
    void testInvalidProductId() {
        Item item = new Item();
        item.setProductName("Product");
        item.setProductId("123456"); // Artikelnummer ist nicht im richtigen Format
        item.setCount(1);
        item.setPrice(50f);

        given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "3")
                .body(item)
                .when()
                .post("/basket/123456")
                .then()
                .statusCode(400)
                .body("parameterViolations.message[0]", containsString("Artikelnummer muss im Format '1-2-3-4-5-6' sein"));
    }

    @Test
    void testInvalidCount() {
        Item item = new Item();
        item.setProductName("Product");
        item.setProductId("1-2-3-4-5-6");
        item.setCount(15); // Anzahl ist zu hoch
        item.setPrice(50f);

        given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "3")
                .body(item)
                .when()
                .post("/basket/1-2-3-4-5-6")
                .then()
                .statusCode(400)
                .body("parameterViolations.message[0]", containsString("Der Inhalt des Warenkorbs darf nicht mehr als 10 Artikel überschreiten"));
    }

    @Test
    void testValidItem() {
        Item item = new Item();
        item.setProductName("Valid Product");
        item.setProductId("1-2-3-4-5-6");
        item.setCount(1);
        item.setPrice(50f);

        given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "3")
                .body(item)
                .when()
                .post("/basket/1-2-3-4-5-6")
                .then()
                .statusCode(201); // Erfolgreiches Hinzufügen
    }
}