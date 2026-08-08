package com.spgg.backend.order;

import jakarta.validation.constraints.NotNull;

public record AssignDeliveryRequest(@NotNull Long deliveryBoyId) {
}
