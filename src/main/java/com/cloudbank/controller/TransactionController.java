package com.cloudbank.controller;

import static com.cloudbank.dto.ApiDtos.PageResponse;
import static com.cloudbank.dto.ApiDtos.TransactionHistoryItem;

import com.cloudbank.entity.UserEntity;
import com.cloudbank.service.TransactionHistoryService;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/transactions")
public class TransactionController {
    private final TransactionHistoryService service;

    public TransactionController(TransactionHistoryService service) {
        this.service = service;
    }

    @GetMapping
    PageResponse<TransactionHistoryItem> history(
        @AuthenticationPrincipal UserEntity user,
        @PathVariable UUID accountId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return service.history(user, accountId, page, size);
    }
}
