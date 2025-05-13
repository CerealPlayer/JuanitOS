package com.juanitos.lib

fun validateQtInt(limit: String): Boolean {
    val limitInt = limit.toIntOrNull()
    return limitInt != null && limitInt >= 0
}

fun validateQtDouble(limit: String): Boolean {
    val limitDouble = limit.toDoubleOrNull()
    return limitDouble != null && limitDouble >= 0
}