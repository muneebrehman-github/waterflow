package com.spgg.backend.order;

import com.spgg.backend.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Customer-facing order placement/history (FR-ORD-001, FR-ORD-009, FR-ORD-014). */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return ApiResponse.ok("Order placed", orderService.placeOrder(request));
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> list() {
        return ApiResponse.ok(orderService.listForCustomer());
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getForCustomer(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable Long id) {
        return ApiResponse.ok("Order cancelled", orderService.cancelByCustomer(id));
    }
}
