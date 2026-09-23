package com.cloudbank.repository;

import com.cloudbank.entity.TransactionEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    Page<TransactionEntity> findByAccountId(UUID accountId, Pageable pageable);
}
