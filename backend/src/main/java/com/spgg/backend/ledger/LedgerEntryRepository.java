package com.spgg.backend.ledger;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(Long customerId);

    Optional<LedgerEntry> findTopByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(Long customerId);
}
