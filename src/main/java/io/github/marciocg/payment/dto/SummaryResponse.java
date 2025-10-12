package io.github.marciocg.payment.dto;
import java.math.BigDecimal;

public record SummaryResponse(long totalRequests, BigDecimal totalAmount) {
    
}
