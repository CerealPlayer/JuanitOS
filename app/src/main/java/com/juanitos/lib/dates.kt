package com.juanitos.lib

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
    return "$day/$month/$year"
}
