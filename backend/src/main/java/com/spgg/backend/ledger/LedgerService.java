package com.spgg.backend.ledger;

import com.spgg.backend.common.TenantContext;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    /**
     * @param signedAmount positive increases what the customer owes, negative decreases it —
     *                     e.g. a CREDIT reversal of a DEBIT passes the same magnitude negated.
     */
    @Transactional
    public LedgerEntry recordEntry(
            Long customerId, LedgerEntryType type, BigDecimal signedAmount, String referenceType,
            Long referenceId, String description) {
        BigDecimal newBalance = getBalance(customerId).add(signedAmount);

        LedgerEntry entry = new LedgerEntry();
        entry.setTenantId(TenantContext.getTenantId());
        entry.setCustomerId(customerId);
        entry.setType(type);
        entry.setAmount(signedAmount);
        entry.setRunningBalanceSnapshot(newBalance);
        entry.setReferenceType(referenceType);
        entry.setReferenceId(referenceId);
        entry.setDescription(description);
        return ledgerEntryRepository.save(entry);
    }

    public BigDecimal getBalance(Long customerId) {
        return ledgerEntryRepository.findTopByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(customerId)
                .map(LedgerEntry::getRunningBalanceSnapshot)
                .orElse(BigDecimal.ZERO);
    }

    public LedgerStatementResponse getStatement(Long customerId) {
        List<LedgerEntry> entries = ledgerEntryRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(customerId);
        BigDecimal balance = entries.isEmpty() ? BigDecimal.ZERO : entries.get(0).getRunningBalanceSnapshot();
        return new LedgerStatementResponse(balance, entries.stream().map(LedgerEntryResponse::from).toList());
    }
}
