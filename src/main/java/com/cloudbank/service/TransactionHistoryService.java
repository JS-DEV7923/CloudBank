package com.cloudbank.service;

import static com.cloudbank.dto.ApiDtos.PageResponse;
import static com.cloudbank.dto.ApiDtos.TransactionHistoryItem;

import com.cloudbank.entity.UserEntity;
import com.cloudbank.exception.ApiException;
import com.cloudbank.repository.AccountRepository;
import com.cloudbank.repository.TransactionRepository;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionHistoryService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionHistoryService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionHistoryItem> history(UserEntity user, UUID accountId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Page must be non-negative and size must be between 1 and 100");
        }
        accountRepository.findByIdAndUserId(accountId, user.getId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account not found"));
        var result = transactionRepository.findByAccountId(accountId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        var items = result.getContent().stream()
            .map(tx -> new TransactionHistoryItem(tx.getId(), tx.getAccount().getId(), tx.getType(), tx.getAmount(), tx.getCurrency(), tx.getBalanceAfter(), tx.getRelatedAccountId(), tx.getTransferId(), tx.getDescription(), tx.getCreatedAt()))
            .toList();
        return new PageResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
