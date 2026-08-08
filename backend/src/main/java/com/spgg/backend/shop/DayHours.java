package com.spgg.backend.shop;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record DayHours(
        DayOfWeek day,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime openTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime closeTime,
        boolean closed) {
}
