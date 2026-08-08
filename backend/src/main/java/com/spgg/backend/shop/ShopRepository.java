package com.spgg.backend.shop;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findByStatusAndDeletedFalse(ShopStatus status);

    List<Shop> findByDeletedFalse();

    Optional<Shop> findByIdAndDeletedFalse(Long id);

    boolean existsByEmailAndDeletedFalse(String email);
}
