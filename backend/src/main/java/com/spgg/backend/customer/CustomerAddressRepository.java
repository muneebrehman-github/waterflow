package com.spgg.backend.customer;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    List<CustomerAddress> findByUserIdAndDeletedFalse(Long userId);

    Optional<CustomerAddress> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    Optional<CustomerAddress> findByIdAndDeletedFalse(Long id);
}
