package com.testora.model;

public record HistoricalReport(long totalRuns, long successfulRuns, double successRate, long minLatencyMillis,
                               long maxLatencyMillis, double avgLatencyMillis) {
}
