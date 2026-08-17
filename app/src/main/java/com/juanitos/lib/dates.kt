package com.juanitos.lib

import java.time.LocalDate
import java.time.YearMonth

/**
 * Convierte un datetime (como el que produce Room: "yyyy-MM-dd HH:mm:ss") a una fecha
 * con formato "dd/MM/yyyy".
 *
 * Ejemplo: "2026-01-14 18:30:01" -> "14/01/2026"
 *
 * Si la entrada no coincide con el patrón esperado, devuelve cadena vacía.
 */
fun formatDbDatetimeToShortDate(datetime: String?): String {
    if (datetime.isNullOrBlank()) return ""

    val input = datetime.trim()
    // Coincide con YYYY-MM-DD al inicio (acepta también "YYYY-MM-DD hh:mm:ss" porque \b corta en espacio)
    val regex = Regex("^(\\d{4})-(\\d{2})-(\\d{2})\\b")
    val match = regex.find(input) ?: return ""
    val (year, month, day) = match.destructured
    // Devolver año con dos dígitos
    val shortYear = if (year.length >= 2) year.takeLast(2) else year
    return "$day/$month/$shortYear"
}

/**
 * Formats a stored time string ("HH:mm:ss") to a short display format ("HH:mm").
 * Returns empty string if input is null or blank.
 */
fun formatTimeToShort(time: String?): String {
    if (time.isNullOrBlank()) return ""
    return time.trim().take(5) // "HH:mm" from "HH:mm:ss"
}

/**
 * Convierte un datetime de Room (formato "yyyy-MM-dd HH:mm:ss") a LocalDate.
 *
 * Ejemplo: "2026-01-14 18:30:01" -> LocalDate(2026, 1, 14)
 *
 * Si la entrada no coincide con el patrón esperado, devuelve null.
 */
fun parseDbDatetimeToLocalDate(datetime: String?): LocalDate? {
    if (datetime.isNullOrBlank()) return null

    val input = datetime.trim()
    val regex = Regex("^(\\d{4})-(\\d{2})-(\\d{2})\\b")
    val match = regex.find(input) ?: return null
    val (year, month, day) = match.destructured
    return try {
        LocalDate.of(year.toInt(), month.toInt(), day.toInt())
    } catch (e: Exception) {
        null
    }
}

/**
 * Clamps a day-of-month into a given YearMonth, so e.g. day 31 in February resolves to
 * Feb 28/29 instead of throwing.
 */
fun clampDayOfMonth(yearMonth: YearMonth, dayOfMonth: Int): LocalDate =
    yearMonth.atDay(minOf(dayOfMonth, yearMonth.lengthOfMonth()))

/**
 * Formats a LocalDate into Room's expected "yyyy-MM-dd HH:mm:ss" datetime string.
 */
fun formatLocalDateToDbDatetime(date: LocalDate, time: String = "00:00:00"): String = "$date $time"

/**
 * Parses a "dd/MM/yyyy" (or "dd/MM/yy", matching [formatDbDatetimeToShortDate]'s output, so a
 * prefilled-but-untouched field round-trips) text input into a LocalDate. Returns null if blank,
 * malformed, or an invalid calendar date.
 */
fun parseShortDateToLocalDate(input: String): LocalDate? {
    if (input.isBlank()) return null

    val match = Regex("^(\\d{1,2})/(\\d{1,2})/(\\d{2}|\\d{4})$").find(input.trim()) ?: return null
    val (day, month, yearText) = match.destructured
    val year = if (yearText.length == 2) 2000 + yearText.toInt() else yearText.toInt()
    return try {
        LocalDate.of(year, month.toInt(), day.toInt())
    } catch (e: Exception) {
        null
    }
}

