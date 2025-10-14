package io.github.marciocg.payment.dto;

public record PaymentsProcessorHealthCheckResponse(boolean failing, int minResponseTime) { }