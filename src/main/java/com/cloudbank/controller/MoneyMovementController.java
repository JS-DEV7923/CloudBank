package com.cloudbank.controller;

import static com.cloudbank.dto.ApiDtos.MoneyRequest;
import static com.cloudbank.dto.ApiDtos.TransactionResponse;
import static com.cloudbank.dto.ApiDtos.TransferRequest;
import static com.cloudbank.dto.ApiDtos.TransferResponse;

import com.cloudbank.entity.UserEntity;
import com.cloudbank.service.MoneyMovementService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MoneyMovementController {
    private final MoneyMovementService service;

    public MoneyMovementController(MoneyMovementService service) {
        this.service = service;
    }

    @PostMapping("/accounts/{accountId}/deposits")
    TransactionResponse deposit(@AuthenticationPrincipal UserEntity user, @PathVariable UUID accountId, @Valid @RequestBody MoneyRequest request, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return service.deposit(user, accountId, request, idempotencyKey);
    }

    @PostMapping("/accounts/{accountId}/withdrawals")
    TransactionResponse withdraw(@AuthenticationPrincipal UserEntity user, @PathVariable UUID accountId, @Valid @RequestBody MoneyRequest request, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return service.withdraw(user, accountId, request, idempotencyKey);
    }

    @PostMapping("/transfers")
    TransferResponse transfer(@AuthenticationPrincipal UserEntity user, @Valid @RequestBody TransferRequest request, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return service.transfer(user, request, idempotencyKey);
    }
}
