package com.spgg.backend.shop;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Shared body shape for both business-hours and delivery-hours updates. */
public record HoursUpdateRequest(@NotEmpty List<DayHours> hours) {
}
