package io.github.marciocg.payment.dto;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(UUID correlationId, BigDecimal amount) {
    
}
