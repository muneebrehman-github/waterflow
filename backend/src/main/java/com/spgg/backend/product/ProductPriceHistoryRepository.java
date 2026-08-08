package com.spgg.backend.product;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistory, Long> {

    List<ProductPriceHistory> findByProductIdOrderByCreatedAtDesc(Long productId);
}
