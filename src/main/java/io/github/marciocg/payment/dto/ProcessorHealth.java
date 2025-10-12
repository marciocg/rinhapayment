package io.github.marciocg.payment.dto;

public record ProcessorHealth(boolean failing, int minResponseTime) {

}
