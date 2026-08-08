package com.spgg.app.domain

/** Mirrors java.time.DayOfWeek's names exactly (the wire format the backend uses). */
enum class DayOfWeek(val label: String) {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    companion object {
        /** Monday-first order for display, matching how the backend validates a full week. */
        val WEEK_ORDER = listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
    }
}
