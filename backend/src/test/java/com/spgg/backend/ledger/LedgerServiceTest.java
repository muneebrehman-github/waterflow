package com.spgg.backend.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.spgg.backend.common.TenantContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        ledgerService = new LedgerService(ledgerEntryRepository);
        TenantContext.setTenantId(7L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private LedgerEntry entry(long id, LedgerEntryType type, BigDecimal amount, BigDecimal balance) {
        LedgerEntry entry = new LedgerEntry();
        entry.setId(id);
        entry.setTenantId(7L);
        entry.setCustomerId(42L);
        entry.setType(type);
        entry.setAmount(amount);
        entry.setRunningBalanceSnapshot(balance);
        entry.setReferenceType("ORDER");
        entry.setReferenceId(100L);
        return entry;
    }

    @Test
    void recordEntry_computesRunningBalanceFromPreviousTop() {
        when(ledgerEntryRepository.findTopByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(42L))
                .thenReturn(Optional.of(entry(1L, LedgerEntryType.DEBIT, BigDecimal.valueOf(600), BigDecimal.valueOf(600))));
        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        when(ledgerEntryRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        ledgerService.recordEntry(42L, LedgerEntryType.CREDIT, BigDecimal.valueOf(-600), "ORDER", 100L, "Payment received");

        LedgerEntry saved = captor.getValue();
        assertThat(saved.getTenantId()).isEqualTo(7L);
        assertThat(saved.getRunningBalanceSnapshot()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getBalance_withNoEntries_returnsZero() {
        when(ledgerEntryRepository.findTopByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(42L)).thenReturn(Optional.empty());

        assertThat(ledgerService.getBalance(42L)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getStatement_returnsBalanceFromMostRecentEntryAndAllEntriesDescending() {
        LedgerEntry newest = entry(2L, LedgerEntryType.DEPOSIT_DEBIT, BigDecimal.valueOf(500), BigDecimal.valueOf(1100));
        LedgerEntry oldest = entry(1L, LedgerEntryType.DEBIT, BigDecimal.valueOf(600), BigDecimal.valueOf(600));
        when(ledgerEntryRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(42L))
                .thenReturn(List.of(newest, oldest));

        LedgerStatementResponse statement = ledgerService.getStatement(42L);

        assertThat(statement.balance()).isEqualByComparingTo(BigDecimal.valueOf(1100));
        assertThat(statement.entries()).hasSize(2);
        assertThat(statement.entries().get(0).id()).isEqualTo(2L);
    }

    @Test
    void getStatement_withNoEntries_returnsZeroBalanceAndEmptyList() {
        when(ledgerEntryRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(42L)).thenReturn(List.of());

        LedgerStatementResponse statement = ledgerService.getStatement(42L);

        assertThat(statement.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(statement.entries()).isEmpty();
    }
}
