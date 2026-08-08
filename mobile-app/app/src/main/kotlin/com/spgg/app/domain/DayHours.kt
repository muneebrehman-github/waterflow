package com.spgg.app.domain

/** openTime/closeTime are "HH:mm" strings (or null when closed) — matches the backend's wire format. */
data class DayHours(
    val day: DayOfWeek,
    val openTime: String?,
    val closeTime: String?,
    val closed: Boolean
)
