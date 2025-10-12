package io.github.marciocg.payment.repository;

import java.time.Instant;
import java.util.List;

import io.github.marciocg.payment.model.Payment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class PaymentRepository {
    @Inject
    EntityManager em;

    public void persist(Payment payment) {
        em.persist(payment);
    }

    public List<Object[]> getSummary(Instant from, Instant to) {
        return em.createQuery("""
            SELECT p.paymentType, COUNT(p), COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.createdAt BETWEEN :from AND :to
            GROUP BY p.paymentType
        """, Object[].class)
        .setParameter("from", from)
        .setParameter("to", to)
        .getResultList();
    }
      
}
