package com.testora.model;

public record ValidationRule(Integer expectedStatus, String bodyContains) {
    public boolean validate(int statusCode, String body) {
        if (expectedStatus != null && statusCode != expectedStatus) {
            return false;
        }
        return bodyContains == null || bodyContains.isBlank() || (body != null && body.contains(bodyContains));
    }
}
