package io.github.marciocg.payment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import io.github.marciocg.payment.model.Payment;
import io.quarkus.test.junit.QuarkusTest;

import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentResourceTest {

    @BeforeAll
    @Transactional
    void cleanup() {
        // remove todas as linhas da tabela payments
        Payment.deleteAll();
    }

    @Test
    void testPostPayment() {
        var payload = Map.of(
                "correlationId", "123e4567-e89b-42d3-a456-556642440000",
                "amount", new BigDecimal("99.99"));

        // ensure previous run data removed so test is repeatable
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/payments")
                .then()
                .statusCode(202);
    }

    @Test
    void testRandomPostPayment() {
        UUID id = UUID.randomUUID();
        double raw = ThreadLocalRandom.current().nextDouble(1.00, 1000.00);
        // round to 2 decimal places
        BigDecimal amount = BigDecimal.valueOf(Math.round(raw * 100.0) / 100.0);

        var payload = Map.of(
                "correlationId", id.toString(),
                "amount", amount);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/payments")
                .then()
                .statusCode(202);
    }

    @Test
    void testSummaryFromDatabase() {
        var from = Instant.now().minusSeconds(3600).toString();
        var to = Instant.now().toString();

        RestAssured.given()
                .queryParam("from", from)
                .queryParam("to", to)
                .get("/payments-summary")
                .then()
                .statusCode(200)
                .body("$", not(empty()));
    }

    // @Test
    // void testSummaryFromRedis() {
    // RestAssured.given()
    // .get("/payments/summary-redis")
    // .then()
    // .statusCode(200)
    // .body("default.totalRequests", notNullValue())
    // .body("default.totalAmount", notNullValue());
    // }
}
