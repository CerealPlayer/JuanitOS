package com.juanitos.ui.food

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class FoodSettingsViewModel : ViewModel() {
    var settingsUiState by mutableStateOf(SettingsUiState())
        private set

    private fun updateUiState(uiState: SettingsUiState) {
        settingsUiState = uiState
    }

    private fun validateQt(limit: String): Boolean {
        val limitInt = limit.toIntOrNull()
        if (limitInt != null && limitInt > 0) {
            return true
        } else {
            return false
        }
    }

    fun setCalorieLimit(calorieLimit: String) {
        updateUiState(settingsUiState.copy(
            calorieLimit = calorieLimit,
            isCalLimitValid = validateQt(calorieLimit)
        ))
    }

    fun setProteinLimit(proteinLimit: String) {
        updateUiState(settingsUiState.copy(
            proteinLimit = proteinLimit,
            isProtLimitValid = validateQt(proteinLimit)
        ))
    }
}

data class SettingsUiState(
    val calorieLimit: String = "",
    val isCalLimitValid: Boolean = true,
    val proteinLimit: String = "",
    val isProtLimitValid: Boolean = true
)