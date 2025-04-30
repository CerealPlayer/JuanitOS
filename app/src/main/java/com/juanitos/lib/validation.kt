package com.juanitos.lib

fun validateQt(limit: String): Boolean {
    val limitInt = limit.toIntOrNull()
    return limitInt != null && limitInt >= 0
}