package com.testora.service;

import com.testora.model.ApiExecutionResult;
import com.testora.model.ApiRequest;
import com.testora.model.ValidationRule;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ApiTestService {
    private final HttpClient client;

    public ApiTestService() {
        this.client = HttpClient.newHttpClient();
    }

    public ApiExecutionResult execute(ApiRequest request, ValidationRule rule) {
        long start = System.nanoTime();
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(request.uri())
                    .timeout(request.timeout());

            request.headers().forEach(builder::header);
            HttpRequest httpRequest = request.body().isBlank()
                    ? builder.method(request.method(), HttpRequest.BodyPublishers.noBody()).build()
                    : builder.method(request.method(), HttpRequest.BodyPublishers.ofString(request.body())).build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long latency = (System.nanoTime() - start) / 1_000_000;
            boolean valid = rule == null || rule.validate(response.statusCode(), response.body());

            return new ApiExecutionResult(
                    response.statusCode(),
                    response.body(),
                    latency,
                    response.statusCode() < 500,
                    valid,
                    Instant.now(),
                    null
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long latency = (System.nanoTime() - start) / 1_000_000;
            return new ApiExecutionResult(0, "", latency, false, false, Instant.now(), e.getMessage());
        } catch (IOException e) {
            long latency = (System.nanoTime() - start) / 1_000_000;
            return new ApiExecutionResult(0, "", latency, false, false, Instant.now(), e.getMessage());
        }
    }

    public List<ApiExecutionResult> executeConcurrent(ApiRequest request, ValidationRule rule, int count, int concurrency) {
        if (count <= 0 || concurrency <= 0) {
            throw new IllegalArgumentException("count and concurrency must be positive");
        }

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        try {
            List<Future<ApiExecutionResult>> futures = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                futures.add(executor.submit(() -> execute(request, rule)));
            }

            List<ApiExecutionResult> results = new ArrayList<>(count);
            for (Future<ApiExecutionResult> future : futures) {
                results.add(future.get());
            }
            return results;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Concurrent execution interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Concurrent execution failed", e);
        } finally {
            executor.shutdownNow();
        }
    }
}
