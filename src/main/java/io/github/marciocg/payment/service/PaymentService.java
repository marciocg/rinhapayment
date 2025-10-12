package io.github.marciocg.payment.service;

import java.time.Instant;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;

import io.github.marciocg.payment.model.Payment;
import io.github.marciocg.payment.repository.PaymentRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentService {

    @Inject
    PaymentRepository repository;

    @Inject
    RedisDataSource redis;

    @Inject
    HealthCheckWorker health;

    @Retry(maxRetries = 3, delay = 100)
    @Fallback(fallbackMethod = "sendToFallback")
    public void sendToDefault(Payment payment) {
        if (!health.isHealthy("default")) {
            System.out.println("[SKIP] Default processor is failing");
            return;
        }
        payment.paymentType = "default";
        payment.createdAt = Instant.now();
        repository.persist(payment);
        saveToRedis(payment);
    }

    public void sendToFallback(Payment payment) {
        if (!health.isHealthy("fallback")) {
            System.out.println("[SKIP] Fallback processor is failing");
            return;
        }
        payment.paymentType = "fallback";
        payment.createdAt = Instant.now();
        repository.persist(payment);
        saveToRedis(payment);
    }

    private void saveToRedis(Payment payment) {
        String key = "summary:" + payment.paymentType;
        redis.execute("HINCRBY", key, "totalRequests", "1");
        redis.execute("HINCRBY", key, "totalAmount", payment.amount.toString());
    }

}
