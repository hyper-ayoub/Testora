# Testora-Desktop-API-Testing-Monitoring-Platform

Testora is a Java desktop application for:
- API testing (single and concurrent request execution)
- automated endpoint monitoring at fixed intervals
- response validation (status/body match rules)
- performance analysis (latency tracking)
- historical reporting (aggregated run statistics)

## Run

```bash
mvn clean test
mvn exec:java -Dexec.mainClass=com.testora.ui.TestoraDesktopApp
```
