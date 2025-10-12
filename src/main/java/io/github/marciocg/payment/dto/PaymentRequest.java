package io.github.marciocg.payment.dto;
import java.math.BigDecimal;

public record PaymentRequest(String correlationId, BigDecimal amount) {
    
}
