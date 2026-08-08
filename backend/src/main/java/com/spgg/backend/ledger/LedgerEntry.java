package com.spgg.backend.ledger;

import com.spgg.backend.common.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BR-008: immutable — corrections are new offsetting entries, never edits.
 * {@code amount} is a signed delta to the running balance (positive = customer
 * owes more, negative = owes less); {@code type} is descriptive/categorical
 * only, not used to derive the sign.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry extends TenantAwareEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LedgerEntryType type;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "running_balance_snapshot", nullable = false)
    private BigDecimal runningBalanceSnapshot;

    @Column(name = "reference_type", nullable = false)
    private String referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "description")
    private String description;
}
