package io.github.marciocg.payment.service;

import java.time.Instant;

import org.eclipse.microprofile.faulttolerance.Fallback;
// import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.github.marciocg.payment.client.DefaultPaymentsProcessor;
import io.github.marciocg.payment.client.FallbackPaymentsProcessor;
import io.github.marciocg.payment.dto.PaymentsProcessorRequest;
import io.github.marciocg.payment.model.Payment;
import io.quarkus.logging.Log;
// import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentService {

    // @Inject
    // RedisDataSource redis;

    @Inject
    HealthCheckService health;

    @Inject
    @RestClient
    DefaultPaymentsProcessor defaultClient;
    @Inject
    @RestClient
    FallbackPaymentsProcessor fallbackClient;

    // @Retry(maxRetries = 2, delay = 100)
    @Fallback(fallbackMethod = "sendToFallbackProcessor")
    public void sendToDefaultProcessor(Payment payment) {
        if (!health.isHealthy("default")) {
            Log.info("[SKIP] Default processor is failing");
            return;
        }
        payment.paymentType = "default";
        payment.createdAt = Instant.now();
        process(payment);
        payment.persist();
        // saveToRedis(payment);
    }

    @Fallback(fallbackMethod = "sendToDefaultProcessor")
    public void sendToFallbackProcessor(Payment payment) {
        if (!health.isHealthy("fallback")) {
            Log.info("[SKIP] Fallback processor is failing");
            return;
        }
        payment.paymentType = "fallback";
        payment.createdAt = Instant.now();
        process(payment);
        payment.persist();
        // saveToRedis(payment);
    }

    private void process(Payment payment) {

        if (payment.paymentType == "default") {
            try {
                var res = defaultClient.processPayment(new PaymentsProcessorRequest(payment.correlationId,
                        payment.amount, payment.createdAt.toString()));
                Log.info("default processor: " + res.message());
            } catch (Exception e) {
                Log.errorf("Error processing payment %s by default processor: %s", payment.correlationId,
                        e.getMessage());
            }

        } else {
            try {
                var res = fallbackClient.processPayment(new PaymentsProcessorRequest(payment.correlationId,
                        payment.amount, payment.createdAt.toString()));
                Log.info("fallback processor: " + res.message());
            } catch (Exception e) {
                Log.errorf("Error processing payment %s by fallback processor: %s", payment.correlationId,
                        e.getMessage());
            }

        }
    }

    /*
     * private void saveToRedis(Payment payment) {
     * String key = "summary:" + payment.paymentType;
     * redis.execute("HINCRBY", key, "totalRequests", "1");
     * redis.execute("HINCRBY", key, "totalAmount", payment.amount.toString());
     * }
     */
}
