package io.github.marciocg.payment.service;

import java.time.Instant;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
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

    @RestClient
    DefaultPaymentsProcessor defaultClient;
    @RestClient
    FallbackPaymentsProcessor fallbackClient;

    @Inject
    PaymentWorker worker;

    @Retry(maxRetries = 2, delay = 200)
    @Fallback(fallbackMethod = "sendToFallbackProcessor")
    public void sendToDefaultProcessor(Payment payment) {
        if (!health.isHealthy("default")) {
            Log.debug("[SKIP] Default processor is failing " + health.toString());
            return;
        }
        payment.paymentType = "default";
        payment.createdAt = Instant.now();
        process(payment);
        try {
          payment.persist();  
        } catch (Exception e) {
            Log.errorf("*** Erro persistindo payment uuid %s by default processor: %s", payment.correlationId, e.getMessage());
        } 
        // saveToRedis(payment);
    }

    @Retry(maxRetries = 2, delay = 200)
    // @Fallback(fallbackMethod = "sendToWorkerQueue")
    @Fallback(fallbackMethod = "enqueueAndProcess")
    public void sendToFallbackProcessor(Payment payment) {
        if (!health.isHealthy("fallback")) {
            Log.debug("[SKIP] Fallback processor is failing" + health.toString());
            return;
        }
        payment.paymentType = "fallback";
        payment.createdAt = Instant.now();
        process(payment);
        try {
          payment.persist();  
        } catch (Exception e) {
            Log.errorf("*** Erro persistindo payment uuid %s by fallback processor: %s", payment.correlationId, e.getMessage());
        } 
        // saveToRedis(payment);
    }

    private void process(Payment payment) {

        if (payment.paymentType == "default") {
            try {
                var res = defaultClient.processPayment(new PaymentsProcessorRequest(payment.correlationId,
                        payment.amount, payment.createdAt.toString()));
                Log.debug("default processor: " + res.message());
            } catch (Exception e) {
                Log.errorf("*** Error processing payment %s by default processor: %s", payment.correlationId,
                        e.getMessage());
            }

        } else {
            try {
                var res = fallbackClient.processPayment(new PaymentsProcessorRequest(payment.correlationId,
                        payment.amount, payment.createdAt.toString()));
                Log.debug("fallback processor: " + res.message());
            } catch (Exception e) {
                Log.errorf("*** Error processing payment %s by fallback processor: %s", payment.correlationId,
                        e.getMessage());
            }

        }
    }

    public void sendToWorkerQueue(Payment payment) {
        Log.warnf("Enqueue payment %s to be processed later", payment.correlationId);
        worker.enqueue(payment);
    }

    public void enqueueAndProcess(Payment payment) {
        worker.enqueueAndProcess(payment);
    }

    /*
     * private void saveToRedis(Payment payment) {
     * String key = "summary:" + payment.paymentType;
     * redis.execute("HINCRBY", key, "totalRequests", "1");
     * redis.execute("HINCRBY", key, "totalAmount", payment.amount.toString());
     * }
     */
}
