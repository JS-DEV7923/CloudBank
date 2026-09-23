package com.cloudbank.dto;

import com.cloudbank.domain.AccountStatus;
import com.cloudbank.domain.TransactionType;
import com.cloudbank.domain.TransferStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record RegisterRequest(
        @Email @NotBlank @Size(max = 180) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName
    ) {
    }

    public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
    ) {
    }

    public record UserResponse(UUID id, String email, String firstName, String lastName, Instant createdAt) {
    }

    public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds, LoginUser user) {
    }

    public record LoginUser(UUID id, String email, String firstName, String lastName) {
    }

    public record AccountResponse(UUID id, String accountNumber, BigDecimal balance, String currency, AccountStatus status, Instant createdAt) {
    }

    public record MoneyRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @Size(max = 280) String description
    ) {
    }

    public record TransferRequest(
        @NotNull UUID sourceAccountId,
        @NotNull UUID destinationAccountId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @Size(max = 280) String description
    ) {
    }

    public record TransactionResponse(
        UUID transactionId,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        BigDecimal balanceAfter,
        Instant createdAt
    ) {
    }

    public record TransferResponse(
        UUID transferId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        TransferStatus status,
        BigDecimal sourceBalanceAfter,
        Instant createdAt
    ) {
    }

    public record TransactionHistoryItem(
        UUID id,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        BigDecimal balanceAfter,
        UUID relatedAccountId,
        UUID transferId,
        String description,
        Instant createdAt
    ) {
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }
}
