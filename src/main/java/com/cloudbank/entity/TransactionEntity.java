package com.cloudbank.entity;

import com.cloudbank.domain.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "transactions",
    indexes = {
        @Index(name = "ix_transactions_account_created_at", columnList = "account_id, created_at"),
        @Index(name = "ix_transactions_transfer_id", columnList = "transfer_id")
    }
)
public class TransactionEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "related_account_id")
    private UUID relatedAccountId;

    @Column(name = "transfer_id")
    private UUID transferId;

    @Column(length = 280)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransactionEntity() {
    }

    public TransactionEntity(AccountEntity account, TransactionType type, BigDecimal amount, UUID relatedAccountId, UUID transferId, String description) {
        this.id = UUID.randomUUID();
        this.account = account;
        this.type = type;
        this.amount = amount.setScale(2);
        this.currency = account.getCurrency();
        this.balanceAfter = account.getBalance();
        this.relatedAccountId = relatedAccountId;
        this.transferId = transferId;
        this.description = description;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public UUID getRelatedAccountId() {
        return relatedAccountId;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
