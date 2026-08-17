package com.juanitos.lib

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DatesTest {

    @Test
    fun clampDayOfMonth_returnsExactDayWhenInRange() {
        assertEquals(LocalDate.of(2026, 1, 14), clampDayOfMonth(YearMonth.of(2026, 1), 14))
    }

    @Test
    fun clampDayOfMonth_clampsToFebNonLeapYear() {
        assertEquals(LocalDate.of(2026, 2, 28), clampDayOfMonth(YearMonth.of(2026, 2), 31))
    }

    @Test
    fun clampDayOfMonth_clampsToFebLeapYear() {
        assertEquals(LocalDate.of(2028, 2, 29), clampDayOfMonth(YearMonth.of(2028, 2), 31))
    }

    @Test
    fun clampDayOfMonth_clampsToThirtyDayMonth() {
        assertEquals(LocalDate.of(2026, 4, 30), clampDayOfMonth(YearMonth.of(2026, 4), 31))
    }

    @Test
    fun formatLocalDateToDbDatetime_usesDefaultMidnightTime() {
        assertEquals(
            "2026-08-17 00:00:00",
            formatLocalDateToDbDatetime(LocalDate.of(2026, 8, 17))
        )
    }

    @Test
    fun formatLocalDateToDbDatetime_acceptsCustomTime() {
        assertEquals(
            "2026-08-17 16:58:46",
            formatLocalDateToDbDatetime(LocalDate.of(2026, 8, 17), "16:58:46")
        )
    }

    @Test
    fun parseShortDateToLocalDate_parsesFourDigitYear() {
        assertEquals(LocalDate.of(2026, 8, 17), parseShortDateToLocalDate("17/08/2026"))
    }

    @Test
    fun parseShortDateToLocalDate_parsesTwoDigitYearMatchingFormatDbDatetimeToShortDate() {
        // Round-trip: formatDbDatetimeToShortDate produces a 2-digit year, so an untouched
        // prefilled field must still parse back correctly.
        val shortDate = formatDbDatetimeToShortDate("2026-08-17 16:58:46")
        assertEquals(LocalDate.of(2026, 8, 17), parseShortDateToLocalDate(shortDate))
    }

    @Test
    fun parseShortDateToLocalDate_returnsNullForBlank() {
        assertNull(parseShortDateToLocalDate(""))
    }

    @Test
    fun parseShortDateToLocalDate_returnsNullForMalformedInput() {
        assertNull(parseShortDateToLocalDate("not a date"))
    }

    @Test
    fun parseShortDateToLocalDate_returnsNullForInvalidCalendarDate() {
        assertNull(parseShortDateToLocalDate("31/02/2026"))
    }
}
