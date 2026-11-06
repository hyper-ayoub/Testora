package com.testora.service;

import com.testora.model.ApiRequest;
import com.testora.model.ValidationRule;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MonitoringService {
    private final ApiTestService apiTestService;
    private final HistoryService historyService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public MonitoringService(ApiTestService apiTestService, HistoryService historyService) {
        this.apiTestService = apiTestService;
        this.historyService = historyService;
    }

    public ScheduledFuture<?> start(ApiRequest request, ValidationRule rule, Duration interval) {
        long delayMs = Math.max(100, interval.toMillis());
        return scheduler.scheduleAtFixedRate(
                () -> historyService.add(apiTestService.execute(request, rule)),
                0,
                delayMs,
                TimeUnit.MILLISECONDS
        );
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
