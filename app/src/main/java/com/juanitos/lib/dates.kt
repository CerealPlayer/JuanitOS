package com.juanitos.lib

import java.time.LocalDate

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

