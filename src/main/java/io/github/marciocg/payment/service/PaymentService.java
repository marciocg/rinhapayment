package io.github.marciocg.payment.service;

import java.time.Instant;

import org.eclipse.microprofile.faulttolerance.Fallback;
// import org.eclipse.microprofile.faulttolerance.Retry;

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

    // @Retry(maxRetries = 2, delay = 100)
    @Fallback(fallbackMethod = "sendToFallbackProcessor")
    public void sendToDefaultProcessor(Payment payment) {
        if (!health.isHealthy("default")) {
            Log.info("[SKIP] Default processor is failing");
            return;
        }
        payment.paymentType = "default";
        payment.createdAt = Instant.now();
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
        payment.persist();
        // saveToRedis(payment);
    }

    /*
     * private void saveToRedis(Payment payment) {
     * String key = "summary:" + payment.paymentType;
     * redis.execute("HINCRBY", key, "totalRequests", "1");
     * redis.execute("HINCRBY", key, "totalAmount", payment.amount.toString());
     * }
     */
}
