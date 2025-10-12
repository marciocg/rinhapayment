package io.github.marciocg.payment.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Payment {
    @Id
    public String correlationId;
    public BigDecimal amount;
    public String paymentType;
    public Instant createdAt;
}
