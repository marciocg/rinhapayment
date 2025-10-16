package io.github.marciocg.payment.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import io.github.marciocg.payment.model.Payment;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentWorker {

    // private final BlockingQueue<Payment> queue = new LinkedBlockingQueue<>();
    private final ConcurrentLinkedQueue<Payment> queue = new ConcurrentLinkedQueue<>();
    // private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Inject
    PaymentService service;

    // @Scheduled(every = "1s")
    void start() {
        // Runnable task = () -> {
            while (!queue.isEmpty()) {
                try {
                    Payment p = queue.poll();
                    if (p != null) {
                        service.sendToDefaultProcessor(p);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        // };
    }

    public void enqueue(Payment payment) {
        queue.offer(payment);
    }

    public void enqueueAndProcess(Payment payment) {
        queue.offer(payment);
        start();
    }

}
