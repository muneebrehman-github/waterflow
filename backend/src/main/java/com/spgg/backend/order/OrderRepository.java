package com.spgg.backend.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(Long customerId);

    List<Order> findByDeletedFalseOrderByCreatedAtDesc();

    Optional<Order> findByIdAndDeletedFalse(Long id);

    Optional<Order> findByIdAndCustomerIdAndDeletedFalse(Long id, Long customerId);

    List<Order> findByDeliveryBoyIdAndStatusAndDeletedFalseOrderByCreatedAtAsc(Long deliveryBoyId, OrderStatus status);

    Optional<Order> findByIdAndDeliveryBoyIdAndDeletedFalse(Long id, Long deliveryBoyId);

    List<Order> findByCustomerIdAndStatusAndDeletedFalse(Long customerId, OrderStatus status);

    List<Order> findByStatusAndDeletedFalse(OrderStatus status);
}
