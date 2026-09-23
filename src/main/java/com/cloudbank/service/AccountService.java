package com.cloudbank.service;

import static com.cloudbank.dto.ApiDtos.AccountResponse;

import com.cloudbank.entity.AccountEntity;
import com.cloudbank.entity.UserEntity;
import com.cloudbank.exception.ApiException;
import com.cloudbank.repository.AccountRepository;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse create(UserEntity user) {
        AccountEntity account = accountRepository.save(new AccountEntity(user, generateAccountNumber()));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> list(UserEntity user) {
        return accountRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse get(UserEntity user, UUID accountId) {
        return toResponse(accountRepository.findByIdAndUserId(accountId, user.getId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account not found")));
    }

    private String generateAccountNumber() {
        for (int i = 0; i < 10; i++) {
            String accountNumber = "10" + (10000000 + secureRandom.nextInt(90000000));
            if (!accountRepository.existsByAccountNumber(accountNumber)) {
                return accountNumber;
            }
        }
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "ACCOUNT_NUMBER_GENERATION_FAILED", "Unable to generate account number");
    }

    public AccountResponse toResponse(AccountEntity account) {
        return new AccountResponse(account.getId(), account.getAccountNumber(), account.getBalance(), account.getCurrency(), account.getStatus(), account.getCreatedAt());
    }
}
