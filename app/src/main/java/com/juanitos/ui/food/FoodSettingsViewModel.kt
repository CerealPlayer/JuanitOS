package com.juanitos.ui.food

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.Setting
import com.juanitos.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodSettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    var settingsUiState by mutableStateOf(SettingsUiState())
        private set

    private fun updateUiState(uiState: SettingsUiState) {
        settingsUiState = uiState
    }

    private fun validateQt(limit: String): Boolean {
        val limitInt = limit.toIntOrNull()
        if (limitInt != null && limitInt >= 0) {
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

    init {
        viewModelScope.launch {
            val calorieLimit = settingsRepository.getByName(name = "calorie").filterNotNull().map {
                it.setting_value
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = "0"
            )
            val proteinLimit = settingsRepository.getByName(name = "protein").filterNotNull().map {
                it.setting_value
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = "0"
            )
            settingsUiState = SettingsUiState(
                calorieLimit = calorieLimit.value,
                isCalLimitValid = validateQt(calorieLimit.value),
                proteinLimit = proteinLimit.value,
                isProtLimitValid = validateQt(proteinLimit.value)
            )
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class SettingsUiState(
    val calorieLimit: String = "",
    val isCalLimitValid: Boolean = true,
    val proteinLimit: String = "",
    val isProtLimitValid: Boolean = true
)
