package com.cloudbank.controller;

import static com.cloudbank.dto.ApiDtos.AccountResponse;

import com.cloudbank.entity.UserEntity;
import com.cloudbank.service.AccountService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AccountResponse create(@AuthenticationPrincipal UserEntity user) {
        return accountService.create(user);
    }

    @GetMapping
    List<AccountResponse> list(@AuthenticationPrincipal UserEntity user) {
        return accountService.list(user);
    }

    @GetMapping("/{accountId}")
    AccountResponse get(@AuthenticationPrincipal UserEntity user, @PathVariable UUID accountId) {
        return accountService.get(user, accountId);
    }
}
