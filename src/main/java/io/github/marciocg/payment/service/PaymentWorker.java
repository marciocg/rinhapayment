package io.github.marciocg.payment.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import io.github.marciocg.payment.model.Payment;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentWorker {

    private final BlockingQueue<Payment> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Inject
    PaymentService service;

    @PostConstruct
    void start() {
        Runnable task = () -> {
            while (true) {
                try {
                    Payment p = queue.take();
                    service.sendToDefaultProcessor(p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        for (int i = 0; i < 8; i++) executor.submit(task);
    }

    public void enqueue(Payment payment) {
        queue.offer(payment);
    }
}
