package com.spgg.backend.payment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(Long customerId);

    Optional<Payment> findByIdAndDeletedFalse(Long id);
}
