package com.juanitos.lib

data class InputUiState(
    val value: String = "",
    val touched: Boolean = false,
    val isValid: Boolean = false,
)
