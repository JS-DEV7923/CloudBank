package com.cloudbank.service;

import static com.cloudbank.dto.ApiDtos.MoneyRequest;
import static com.cloudbank.dto.ApiDtos.TransactionResponse;
import static com.cloudbank.dto.ApiDtos.TransferRequest;
import static com.cloudbank.dto.ApiDtos.TransferResponse;

import com.cloudbank.domain.TransactionType;
import com.cloudbank.entity.AccountEntity;
import com.cloudbank.entity.TransactionEntity;
import com.cloudbank.entity.TransferEntity;
import com.cloudbank.entity.UserEntity;
import com.cloudbank.exception.ApiException;
import com.cloudbank.repository.AccountRepository;
import com.cloudbank.repository.TransactionRepository;
import com.cloudbank.repository.TransferRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoneyMovementService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final IdempotencyService idempotencyService;

    public MoneyMovementService(AccountRepository accountRepository, TransactionRepository transactionRepository, TransferRepository transferRepository, IdempotencyService idempotencyService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    public TransactionResponse deposit(UserEntity user, UUID accountId, MoneyRequest request, String idempotencyKey) {
        return idempotencyService.execute(user, "POST /api/v1/accounts/{accountId}/deposits", idempotencyKey, Map.of("accountId", accountId, "request", request), TransactionResponse.class, () -> {
            AccountEntity account = ownedAccountForUpdate(accountId, user.getId());
            BigDecimal amount = normalizeAmount(request.amount());
            account.setBalance(account.getBalance().add(amount));
            TransactionEntity transaction = transactionRepository.save(new TransactionEntity(account, TransactionType.DEPOSIT, amount, null, null, request.description()));
            return toTransactionResponse(transaction);
        });
    }

    @Transactional
    public TransactionResponse withdraw(UserEntity user, UUID accountId, MoneyRequest request, String idempotencyKey) {
        return idempotencyService.execute(user, "POST /api/v1/accounts/{accountId}/withdrawals", idempotencyKey, Map.of("accountId", accountId, "request", request), TransactionResponse.class, () -> {
            AccountEntity account = ownedAccountForUpdate(accountId, user.getId());
            BigDecimal amount = normalizeAmount(request.amount());
            ensureSufficientFunds(account, amount);
            account.setBalance(account.getBalance().subtract(amount));
            TransactionEntity transaction = transactionRepository.save(new TransactionEntity(account, TransactionType.WITHDRAWAL, amount, null, null, request.description()));
            return toTransactionResponse(transaction);
        });
    }

    @Transactional
    public TransferResponse transfer(UserEntity user, TransferRequest request, String idempotencyKey) {
        return idempotencyService.execute(user, "POST /api/v1/transfers", idempotencyKey, request, TransferResponse.class, () -> {
            BigDecimal amount = normalizeAmount(request.amount());
            List<AccountEntity> locked = List.of(request.sourceAccountId(), request.destinationAccountId()).stream()
                .distinct()
                .sorted(Comparator.comparing(UUID::toString))
                .map(id -> accountRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account not found")))
                .toList();
            AccountEntity source = locked.stream()
                .filter(account -> account.getId().equals(request.sourceAccountId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SOURCE_ACCOUNT_NOT_FOUND", "Source account not found"));
            AccountEntity destination = locked.stream()
                .filter(account -> account.getId().equals(request.destinationAccountId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "DESTINATION_ACCOUNT_NOT_FOUND", "Destination account not found"));
            if (!source.getUser().getId().equals(user.getId())) {
                throw new ApiException(HttpStatus.NOT_FOUND, "SOURCE_ACCOUNT_NOT_FOUND", "Source account not found");
            }
            ensureSufficientFunds(source, amount);
            source.setBalance(source.getBalance().subtract(amount));
            destination.setBalance(destination.getBalance().add(amount));
            TransferEntity transfer = transferRepository.save(new TransferEntity(source, destination, amount));
            transactionRepository.save(new TransactionEntity(source, TransactionType.TRANSFER_DEBIT, amount, destination.getId(), transfer.getId(), request.description()));
            transactionRepository.save(new TransactionEntity(destination, TransactionType.TRANSFER_CREDIT, amount, source.getId(), transfer.getId(), request.description()));
            return new TransferResponse(transfer.getId(), source.getId(), destination.getId(), amount, source.getCurrency(), transfer.getStatus(), source.getBalance(), transfer.getCreatedAt());
        });
    }

    private AccountEntity ownedAccountForUpdate(UUID accountId, UUID userId) {
        AccountEntity account = accountRepository.findByIdForUpdate(accountId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account not found"));
        if (!account.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account not found");
        }
        return account;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private void ensureSufficientFunds(AccountEntity account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_FUNDS", "Insufficient funds");
        }
    }

    private TransactionResponse toTransactionResponse(TransactionEntity transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getAccount().getId(), transaction.getType(), transaction.getAmount(), transaction.getCurrency(), transaction.getBalanceAfter(), transaction.getCreatedAt());
    }
}
