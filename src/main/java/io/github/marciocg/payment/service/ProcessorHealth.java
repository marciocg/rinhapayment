package io.github.marciocg.payment.service;

public class ProcessorHealth {
    
    public boolean failing;
    public int minResponseTime;

    public ProcessorHealth(boolean failing, int minResponseTime) {
        this.failing = failing;
        this.minResponseTime = minResponseTime;
    }
}
