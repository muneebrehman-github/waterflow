package com.spgg.backend.ledger;

import java.math.BigDecimal;
import java.util.List;

public record LedgerStatementResponse(BigDecimal balance, List<LedgerEntryResponse> entries) {
}
