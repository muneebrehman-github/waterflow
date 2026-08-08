package com.spgg.backend.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PlaceOrderRequest(
        @NotNull Long addressId,
        @NotEmpty List<OrderItemRequest> items,
        @Size(max = 500) String deliveryNotes) {
}
