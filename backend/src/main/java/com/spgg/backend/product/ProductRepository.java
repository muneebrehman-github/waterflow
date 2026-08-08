package com.spgg.backend.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByDeletedFalseOrderByDisplayOrderAsc();

    List<Product> findByStatusAndDeletedFalseOrderByDisplayOrderAsc(ProductStatus status);

    Optional<Product> findByIdAndDeletedFalse(Long id);
}
