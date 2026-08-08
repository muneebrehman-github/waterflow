package com.spgg.backend.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderIdAndDeletedFalse(Long orderId);
}
