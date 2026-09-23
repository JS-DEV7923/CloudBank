package com.cloudbank.repository;

import com.cloudbank.entity.IdempotencyRecord;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, UUID> {
    Optional<IdempotencyRecord> findByUserIdAndEndpointAndIdempotencyKey(UUID userId, String endpoint, String idempotencyKey);
}
