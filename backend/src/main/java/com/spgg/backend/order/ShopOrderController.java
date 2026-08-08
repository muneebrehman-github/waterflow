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

/** Shop Owner/Staff order management (FR-ORD-006/007/008/015). */
@RestController
@RequestMapping("/api/v1/shop/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SHOP_OWNER','STAFF')")
public class ShopOrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<List<OrderResponse>> list() {
        return ApiResponse.ok(orderService.listForShop());
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getForShop(id));
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<OrderResponse> accept(@PathVariable Long id) {
        return ApiResponse.ok("Order accepted", orderService.accept(id));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<OrderResponse> reject(@PathVariable Long id, @Valid @RequestBody OrderReasonRequest request) {
        return ApiResponse.ok("Order rejected", orderService.reject(id, request.reason()));
    }

    @PostMapping("/{id}/prepare")
    public ApiResponse<OrderResponse> prepare(@PathVariable Long id) {
        return ApiResponse.ok("Order marked as preparing", orderService.markPreparing(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable Long id, @Valid @RequestBody OrderReasonRequest request) {
        return ApiResponse.ok("Order cancelled", orderService.cancelByShop(id, request.reason()));
    }

    @PostMapping("/{id}/assign-delivery")
    public ApiResponse<OrderResponse> assignDelivery(@PathVariable Long id, @Valid @RequestBody AssignDeliveryRequest request) {
        return ApiResponse.ok("Delivery assigned", orderService.assignDelivery(id, request.deliveryBoyId()));
    }
}
