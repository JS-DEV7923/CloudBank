package com.cloudbank.service;

import com.cloudbank.entity.IdempotencyRecord;
import com.cloudbank.entity.UserEntity;
import com.cloudbank.exception.ApiException;
import com.cloudbank.repository.IdempotencyRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {
    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public <T> T execute(UserEntity user, String endpoint, String key, Object requestPayload, Class<T> responseType, Supplier<T> operation) {
        if (key == null || key.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key header is required");
        }
        String requestHash = hash(requestPayload);
        return repository.findByUserIdAndEndpointAndIdempotencyKey(user.getId(), endpoint, key)
            .map(record -> replay(record, requestHash, responseType))
            .orElseGet(() -> executeAndStore(user, endpoint, key, requestHash, operation, responseType));
    }

    private <T> T executeAndStore(UserEntity user, String endpoint, String key, String requestHash, Supplier<T> operation, Class<T> responseType) {
        T response = operation.get();
        try {
            repository.saveAndFlush(new IdempotencyRecord(user, key, endpoint, requestHash, 200, objectMapper.writeValueAsString(response)));
            return response;
        } catch (DataIntegrityViolationException ex) {
            return repository.findByUserIdAndEndpointAndIdempotencyKey(user.getId(), endpoint, key)
                .map(record -> replay(record, requestHash, responseType))
                .orElseThrow(() -> ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to store idempotency record", ex);
        }
    }

    private <T> T replay(IdempotencyRecord record, String requestHash, Class<T> responseType) {
        if (!record.getRequestHash().equals(requestHash)) {
            throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_CONFLICT", "Idempotency key was reused with a different request");
        }
        try {
            return objectMapper.readValue(record.getResponseBody(), responseType);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to replay idempotent response", ex);
        }
    }

    private String hash(Object value) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(Map.of("payload", value));
            return sha256(json);
        } catch (Exception ex) {
            return sha256(String.valueOf(value).getBytes(StandardCharsets.UTF_8));
        }
    }

    private String sha256(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash idempotency request", ex);
        }
    }
}
