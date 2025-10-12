package io.github.marciocg.payment.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.rest.client.inject.RestClient;

// import jakarta.inject.Singleton;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.github.marciocg.payment.client.DefaultHealthCheckPaymentsProcessor;
import io.github.marciocg.payment.client.FallbackHealthCheckPaymentsProcessor;
import io.github.marciocg.payment.dto.ProcessorHealth;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class HealthCheckService {

    private final Map<String, ProcessorHealth> healthMap = new ConcurrentHashMap<>();

    @Inject
    @RestClient
    DefaultHealthCheckPaymentsProcessor defaultClient;
    @Inject
    @RestClient
    FallbackHealthCheckPaymentsProcessor fallbackClient;

    @Scheduled(every = "PT0.5s")
    void checkHealth() {
        try {
            var res = defaultClient.getHealth();
            setHealth("default", res.failing(), res.minResponseTime());
            // healthMap.put("default", new ProcessorHealth(res.failing(),
            // res.minResponseTime()));

        } catch (Exception e) {
            healthMap.put("default", new ProcessorHealth(true, 0));
        }

        try {
            var res = fallbackClient.getHealth();
            setHealth("fallback", res.failing(), res.minResponseTime());
            // healthMap.put("fallback", new ProcessorHealth(res.failing(),
            // res.minResponseTime()));

        } catch (Exception e) {
            healthMap.put("fallback", new ProcessorHealth(true, 0));
        }
    }

    public boolean isHealthy(String processor) {
        return Optional.ofNullable(healthMap.get(processor))
                .map(h -> !h.failing())
                .orElse(false);
    }

    public void setHealth(String processor, boolean failing, int minResponseTime) {
        healthMap.put(processor, new ProcessorHealth(failing, minResponseTime));
    }
}
