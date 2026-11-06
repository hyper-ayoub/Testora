package com.testora.service;

import com.testora.model.ApiExecutionResult;
import com.testora.model.HistoricalReport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HistoryService {
    private final CopyOnWriteArrayList<ApiExecutionResult> history = new CopyOnWriteArrayList<>();

    public void add(ApiExecutionResult result) {
        history.add(result);
    }

    public List<ApiExecutionResult> findAll() {
        return new ArrayList<>(history);
    }

    public HistoricalReport buildReport() {
        if (history.isEmpty()) {
            return new HistoricalReport(0, 0, 0.0, 0, 0, 0.0);
        }

        long total = history.size();
        long successful = history.stream().filter(ApiExecutionResult::successful).count();
        long min = history.stream().mapToLong(ApiExecutionResult::latencyMillis).min().orElse(0);
        long max = history.stream().mapToLong(ApiExecutionResult::latencyMillis).max().orElse(0);
        double avg = history.stream().mapToLong(ApiExecutionResult::latencyMillis).average().orElse(0.0);

        return new HistoricalReport(total, successful, (successful * 100.0) / total, min, max, avg);
    }
}
