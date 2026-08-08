package com.spgg.backend.order;

import jakarta.validation.constraints.Min;

public record CompleteDeliveryRequest(@Min(0) Integer emptyBottlesCollected) {
}
