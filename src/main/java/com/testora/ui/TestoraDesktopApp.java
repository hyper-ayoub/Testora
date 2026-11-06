package com.testora.ui;

import com.testora.model.ApiExecutionResult;
import com.testora.model.ApiRequest;
import com.testora.model.HistoricalReport;
import com.testora.model.ValidationRule;
import com.testora.service.ApiTestService;
import com.testora.service.HistoryService;
import com.testora.service.MonitoringService;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class TestoraDesktopApp {
    private final ApiTestService apiTestService = new ApiTestService();
    private final HistoryService historyService = new HistoryService();
    private final MonitoringService monitoringService = new MonitoringService(apiTestService, historyService);
    private ScheduledFuture<?> monitorHandle;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TestoraDesktopApp().createAndShow());
    }

    private void createAndShow() {
        JFrame frame = new JFrame("Testora API Testing & Monitoring");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(820, 520);

        JTextField urlField = new JTextField("https://postman-echo.com/get?testora=true", 45);
        JTextField methodField = new JTextField("GET", 8);
        JTextField expectedStatusField = new JTextField("200", 4);
        JTextField bodyContainsField = new JTextField("testora", 12);
        JTextField concurrentCountField = new JTextField("5", 4);
        JTextField intervalMsField = new JTextField("2000", 6);
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);

        JButton runSingleButton = new JButton("Run Test");
        JButton runConcurrentButton = new JButton("Run Concurrent");
        JButton monitorButton = new JButton("Start Monitoring");
        JButton reportButton = new JButton("Show Historical Report");

        runSingleButton.addActionListener(e -> {
            ApiRequest request = buildRequest(urlField.getText(), methodField.getText());
            ValidationRule rule = buildRule(expectedStatusField.getText(), bodyContainsField.getText());
            ApiExecutionResult result = apiTestService.execute(request, rule);
            historyService.add(result);
            outputArea.append(formatResult("Single", result));
        });

        runConcurrentButton.addActionListener(e -> {
            ApiRequest request = buildRequest(urlField.getText(), methodField.getText());
            ValidationRule rule = buildRule(expectedStatusField.getText(), bodyContainsField.getText());
            int count = Integer.parseInt(concurrentCountField.getText().trim());
            List<ApiExecutionResult> results = apiTestService.executeConcurrent(request, rule, count, Math.min(count, 10));
            results.forEach(historyService::add);
            long success = results.stream().filter(ApiExecutionResult::successful).count();
            outputArea.append("Concurrent run completed: " + success + "/" + results.size() + " successful\n");
        });

        monitorButton.addActionListener(e -> {
            if (monitorHandle == null || monitorHandle.isCancelled()) {
                ApiRequest request = buildRequest(urlField.getText(), methodField.getText());
                ValidationRule rule = buildRule(expectedStatusField.getText(), bodyContainsField.getText());
                monitorHandle = monitoringService.start(request, rule, Duration.ofMillis(Long.parseLong(intervalMsField.getText().trim())));
                monitorButton.setText("Stop Monitoring");
                outputArea.append("Monitoring started\n");
            } else {
                monitorHandle.cancel(true);
                monitorButton.setText("Start Monitoring");
                outputArea.append("Monitoring stopped\n");
            }
        });

        reportButton.addActionListener(e -> {
            HistoricalReport report = historyService.buildReport();
            outputArea.append("Report -> total=" + report.totalRuns() + ", successRate="
                    + String.format("%.2f", report.successRate()) + "%, latency(ms) min/avg/max="
                    + report.minLatencyMillis() + "/" + String.format("%.2f", report.avgLatencyMillis())
                    + "/" + report.maxLatencyMillis() + "\n");
        });

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("URL:"));
        panel.add(urlField);
        panel.add(new JLabel("Method:"));
        panel.add(methodField);
        panel.add(new JLabel("Expected:"));
        panel.add(expectedStatusField);
        panel.add(new JLabel("Contains:"));
        panel.add(bodyContainsField);
        panel.add(new JLabel("Concurrent:"));
        panel.add(concurrentCountField);
        panel.add(new JLabel("Interval(ms):"));
        panel.add(intervalMsField);
        panel.add(runSingleButton);
        panel.add(runConcurrentButton);
        panel.add(monitorButton);
        panel.add(reportButton);

        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        frame.setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread(monitoringService::shutdown));
    }

    private ApiRequest buildRequest(String url, String method) {
        return new ApiRequest(method, URI.create(url), null, "", Duration.ofSeconds(15));
    }

    private ValidationRule buildRule(String expectedStatus, String contains) {
        Integer expected = expectedStatus == null || expectedStatus.isBlank() ? null : Integer.parseInt(expectedStatus.trim());
        return new ValidationRule(expected, contains);
    }

    private String formatResult(String label, ApiExecutionResult result) {
        return label + " -> status=" + result.statusCode()
                + ", latency=" + result.latencyMillis()
                + "ms, validation=" + result.validationPassed()
                + (result.errorMessage() == null ? "" : ", error=" + result.errorMessage())
                + "\n";
    }
}
