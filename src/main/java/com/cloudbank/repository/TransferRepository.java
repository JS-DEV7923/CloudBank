package com.cloudbank.repository;

import com.cloudbank.entity.TransferEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<TransferEntity, UUID> {
}
