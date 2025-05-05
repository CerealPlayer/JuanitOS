package com.juanitos.ui.food.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.repositories.SettingsRepository
import com.juanitos.lib.validateQt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FoodSettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    var settingsUiState by mutableStateOf(SettingsUiState())
        private set

    val initialCalorieLimit = settingsRepository.getByName(name = "calorie").filterNotNull().map {
        it.settingValue
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = "0"
    )
    val initialProteinLimit = settingsRepository.getByName(name = "protein").filterNotNull().map {
        it.settingValue
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = "0"
    )


    private fun updateUiState(uiState: SettingsUiState) {
        settingsUiState = uiState
    }

    fun setCalorieLimit(calorieLimit: String) {
        updateUiState(
            settingsUiState.copy(
                calorieLimit = calorieLimit,
                isCalLimitValid = validateQt(calorieLimit),
                isCalEdited = true
            )
        )
    }

    fun setProteinLimit(proteinLimit: String) {
        updateUiState(
            settingsUiState.copy(
                proteinLimit = proteinLimit,
                isProtLimitValid = validateQt(proteinLimit),
                isProtEdited = true
            )
        )
    }

    suspend fun saveSettings() {
        if (validateQt(settingsUiState.calorieLimit) && validateQt(settingsUiState.proteinLimit)) {
            settingsRepository.insertSetting(
                name = "calorie",
                value = settingsUiState.calorieLimit.toIntOrNull()?.toString() ?: "0",
            )
            settingsRepository.insertSetting(
                name = "protein",
                value = settingsUiState.proteinLimit.toIntOrNull()?.toString() ?: "0"
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
    val isProtLimitValid: Boolean = true,
    val isCalEdited: Boolean = false,
    val isProtEdited: Boolean = false
)
