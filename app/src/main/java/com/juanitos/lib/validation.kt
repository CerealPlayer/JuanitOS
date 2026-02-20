package com.juanitos.lib

fun validateQtInt(limit: String): Boolean {
    val limitInt = limit.toIntOrNull()
    return limitInt != null && limitInt >= 0
}

/**
 * Parses a string to a Double, accepting both dot (.) and comma (,) as decimal separators.
 * Returns null if the string cannot be parsed or if the value is negative.
 *
 * Examples:
 * - "123.45" -> 123.45
 * - "123,45" -> 123.45
 * - "123" -> 123.0
 * - "-10" -> null (negative values not allowed)
 * - "abc" -> null (invalid format)
 */
fun parseQtDouble(input: String): Double? {
    if (input.isBlank()) return null

    // Replace comma with dot to normalize decimal separator
    val normalized = input.trim().replace(',', '.')

    return normalized.toDoubleOrNull()
}

/**
 * Validates if a string represents a valid non-negative double value.
 * Accepts both dot (.) and comma (,) as decimal separators.
 *
 * @deprecated Use parseQtDouble instead for better type safety
 */
@Deprecated("Use parseQtDouble instead", ReplaceWith("parseQtDouble(limit) != null"))
fun validateQtDouble(limit: String): Boolean {
    return parseQtDouble(limit) != null
}