package com.testora.model;

import java.time.Instant;

public record ApiExecutionResult(
        int statusCode,
        String responseBody,
        long latencyMillis,
        boolean successful,
        boolean validationPassed,
        Instant timestamp,
        String errorMessage
) {
}
