package io.github.marciocg.payment.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;

@ApplicationScoped
public class HealthCheckWorker {

    private final Map<String, ProcessorHealth> healthMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    void start() {
        scheduler.scheduleAtFixedRate(this::checkHealth, 0, 1, TimeUnit.SECONDS);
    }

    private void checkHealth() {
        checkProcessor("default", "http://localhost:8001/payments/service-health");
        checkProcessor("fallback", "http://localhost:8002/payments/service-health");
    }

    private void checkProcessor(String name, String url) {
        try {
            URI uri = URI.create(url);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(500);
            conn.setReadTimeout(500);

            if (conn.getResponseCode() == 200) {
                try (var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String json = reader.lines().collect(Collectors.joining());
                    var node = Json.createReader(new StringReader(json)).readObject();
                    boolean failing = node.getBoolean("failing");
                    int minResponseTime = node.getInt("minResponseTime");
                    healthMap.put(name, new ProcessorHealth(failing, minResponseTime));
                }
            } else {
                healthMap.put(name, new ProcessorHealth(true, 0));
            }
        } catch (Exception e) {
            healthMap.put(name, new ProcessorHealth(true, 0));
        }
    }

    public boolean isHealthy(String processor) {
        return Optional.ofNullable(healthMap.get(processor))
                .map(h -> !h.failing)
                .orElse(false);
    }

}
