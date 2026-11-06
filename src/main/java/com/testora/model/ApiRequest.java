package com.testora.model;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

public record ApiRequest(String method, URI uri, Map<String, String> headers, String body, Duration timeout) {
    public ApiRequest {
        method = method == null ? "GET" : method.toUpperCase();
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        timeout = timeout == null ? Duration.ofSeconds(10) : timeout;
        body = body == null ? "" : body;
    }
}
