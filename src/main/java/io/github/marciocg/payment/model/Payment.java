package io.github.marciocg.payment.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.github.marciocg.payment.dto.SummaryResponseByPaymentType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_created_at", columnList = "createdAt") //, @Index(name = "idx_payment_payment_type", columnList = "paymentType")
})
@NamedQueries({
    @NamedQuery(
        name = "Payment.getSummaryByPaymentType",
        query = "SELECT p.paymentType, COUNT(p), COALESCE(SUM(p.amount), 0) " +
                "FROM Payment p " +
                "WHERE p.createdAt BETWEEN :from AND :to " +
                "GROUP BY p.paymentType"
    )
})
public class Payment extends PanacheEntityBase {
    @Id
    public UUID correlationId;
    public BigDecimal amount;
    public String paymentType;
    public Instant createdAt;

    public static List<SummaryResponseByPaymentType> getSummaryByPaymentType(Instant from, Instant to) {
        PanacheQuery<SummaryResponseByPaymentType> query = find("Payment.getSummaryByPaymentType", Parameters.with("from", from).and("to", to)).project(SummaryResponseByPaymentType.class);
        return query.list();
    }

}
