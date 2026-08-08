package com.spgg.backend.ledger;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntryResponse(
        Long id,
        LedgerEntryType type,
        BigDecimal amount,
        BigDecimal runningBalanceSnapshot,
        String referenceType,
        Long referenceId,
        String description,
        Instant createdAt) {

    public static LedgerEntryResponse from(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getType(),
                entry.getAmount(),
                entry.getRunningBalanceSnapshot(),
                entry.getReferenceType(),
                entry.getReferenceId(),
                entry.getDescription(),
                entry.getCreatedAt());
    }
}
