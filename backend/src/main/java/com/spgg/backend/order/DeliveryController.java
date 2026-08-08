package com.spgg.backend.order;

import com.spgg.backend.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-DEL-002/003/004: Delivery Boy's view of orders assigned to them. */
@RestController
@RequestMapping("/api/v1/delivery/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DELIVERY_BOY')")
public class DeliveryController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<List<OrderResponse>> list() {
        return ApiResponse.ok(orderService.listForDeliveryBoy());
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getForDeliveryBoy(id));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<OrderResponse> complete(@PathVariable Long id, @Valid @RequestBody CompleteDeliveryRequest request) {
        return ApiResponse.ok("Delivery completed", orderService.completeDelivery(id, request.emptyBottlesCollected()));
    }
}
