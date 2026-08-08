package com.spgg.backend.bottle;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BottleAdjustmentRepository extends JpaRepository<BottleAdjustment, Long> {

    List<BottleAdjustment> findByCustomerIdAndDeletedFalse(Long customerId);

    List<BottleAdjustment> findByDeletedFalse();
}
