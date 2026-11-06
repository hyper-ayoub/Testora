package com.testora.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.testora.model.ApiExecutionResult;
import com.testora.model.ApiRequest;
import com.testora.model.HistoricalReport;
import com.testora.model.ValidationRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiTestServiceTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void executeAppliesValidationAndCapturesLatency() throws Exception {
        server = createServer("{\"status\":\"ok\"}", 200);

        ApiTestService service = new ApiTestService();
        ApiRequest request = new ApiRequest("GET", URI.create("http://localhost:" + server.getAddress().getPort() + "/ping"), null, "", Duration.ofSeconds(2));
        ValidationRule rule = new ValidationRule(200, "ok");

        ApiExecutionResult result = service.execute(request, rule);

        assertEquals(200, result.statusCode());
        assertTrue(result.successful());
        assertTrue(result.validationPassed());
        assertTrue(result.latencyMillis() >= 0);
    }

    @Test
    void executeConcurrentReturnsRequestedNumberOfResults() throws Exception {
        server = createServer("pong", 200);

        ApiTestService service = new ApiTestService();
        ApiRequest request = new ApiRequest("GET", URI.create("http://localhost:" + server.getAddress().getPort() + "/concurrent"), null, "", Duration.ofSeconds(2));

        List<ApiExecutionResult> results = service.executeConcurrent(request, new ValidationRule(200, "pong"), 8, 4);

        assertEquals(8, results.size());
        assertEquals(8, results.stream().filter(ApiExecutionResult::validationPassed).count());
    }

    @Test
    void historyReportCalculatesSuccessAndLatencyStats() {
        HistoryService historyService = new HistoryService();
        historyService.add(new ApiExecutionResult(200, "ok", 10, true, true, java.time.Instant.now(), null));
        historyService.add(new ApiExecutionResult(500, "boom", 30, false, false, java.time.Instant.now(), "error"));

        HistoricalReport report = historyService.buildReport();

        assertEquals(2, report.totalRuns());
        assertEquals(1, report.successfulRuns());
        assertEquals(50.0, report.successRate());
        assertEquals(10, report.minLatencyMillis());
        assertEquals(30, report.maxLatencyMillis());
        assertEquals(20.0, report.avgLatencyMillis());
    }

    @Test
    void monitoringRunsRepeatedChecksIntoHistory() throws Exception {
        server = createServer("monitor", 200);

        ApiTestService apiTestService = new ApiTestService();
        HistoryService historyService = new HistoryService();
        MonitoringService monitoringService = new MonitoringService(apiTestService, historyService);

        ApiRequest request = new ApiRequest("GET", URI.create("http://localhost:" + server.getAddress().getPort() + "/monitor"), null, "", Duration.ofSeconds(2));
        var handle = monitoringService.start(request, new ValidationRule(200, "monitor"), Duration.ofMillis(100));
        Thread.sleep(350);
        handle.cancel(true);
        monitoringService.shutdown();

        assertTrue(historyService.findAll().size() >= 2);
    }

    private HttpServer createServer(String body, int status) throws IOException {
        HttpServer localServer = HttpServer.create(new InetSocketAddress(0), 0);
        localServer.createContext("/", exchange -> respond(exchange, body, status));
        localServer.setExecutor(null);
        localServer.start();
        return localServer;
    }

    private void respond(HttpExchange exchange, String body, int status) throws IOException {
        exchange.sendResponseHeaders(status, body.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        }
    }
}
