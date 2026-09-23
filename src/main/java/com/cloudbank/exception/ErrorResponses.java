package com.cloudbank.exception;

import java.time.Instant;
import java.util.List;

public final class ErrorResponses {
    private ErrorResponses() {
    }

    public record FieldErrorResponse(String field, String message) {
    }

    public record ApiErrorResponse(
        int status,
        String code,
        String message,
        List<FieldErrorResponse> fieldErrors,
        String path,
        Instant timestamp,
        String correlationId
    ) {
    }
}
