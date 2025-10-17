package io.github.marciocg.payment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class PaymentResourceTest {

    @Test
    void testPostPayment() {
        var payload = Map.of(
            "correlationId", "123e4567-e89b-42d3-a456-556642440000",
            "amount", new BigDecimal("99.99")
        );

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(payload)
            .post("/payments")
            .then()
            .statusCode(201);
    }

    @Test
    void testSummaryFromDatabase() {
        var from = Instant.now().minusSeconds(3600).toString();
        var to = Instant.now().toString();

        RestAssured.given()
            .queryParam("from", from)
            .queryParam("to", to)
            .get("/payments/summary")
            .then()
            .statusCode(200)
            .body("$", not(empty()));
    }

    @Test
    void testSummaryFromRedis() {
        RestAssured.given()
            .get("/payments/summary-redis")
            .then()
            .statusCode(200)
            .body("default.totalRequests", notNullValue())
            .body("default.totalAmount", notNullValue());
    }
}
