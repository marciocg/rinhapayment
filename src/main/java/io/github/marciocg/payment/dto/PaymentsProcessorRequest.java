package io.github.marciocg.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentsProcessorRequest(
    UUID correlationId,
    BigDecimal amount,
    String createdAt
) { }
