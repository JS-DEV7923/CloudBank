package com.cloudbank.exception;

import static com.cloudbank.exception.ErrorResponses.ApiErrorResponse;
import static com.cloudbank.exception.ErrorResponses.FieldErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiErrorResponse> handleApi(ApiException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatus()).body(error(ex.getStatus(), ex.getCode(), ex.getMessage(), List.of(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorResponse> fields = ex.getBindingResult().getFieldErrors().stream()
            .map(field -> new FieldErrorResponse(field.getField(), field.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest().body(error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", fields, request));
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    ResponseEntity<ApiErrorResponse> handleAuth(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials", List.of(), request));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Unexpected server error", List.of(), request));
    }

    private ApiErrorResponse error(HttpStatus status, String code, String message, List<FieldErrorResponse> fields, HttpServletRequest request) {
        return new ApiErrorResponse(status.value(), code, message, fields, request.getRequestURI(), Instant.now(), MDC.get("correlationId"));
    }
}
