package com.juanitos.ui.food

import android.util.Log
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

    suspend fun saveSettings() {
        if (validateQt(settingsUiState.calorieLimit) && validateQt(settingsUiState.proteinLimit)) {
            settingsRepository.insertSetting(
                Setting(
                    setting_name = "calorie",
                    setting_value = settingsUiState.calorieLimit.toIntOrNull()?.toString() ?: "0",
                )
            )
            settingsRepository.insertSetting(
                Setting(
                    setting_name = "protein",
                    setting_value = settingsUiState.proteinLimit.toIntOrNull()?.toString() ?: "0"
                )
            )
        }
    }

    fun getInitialCalorieLimit(): String {
        return settingsRepository.getByName(name = "calorie").filterNotNull().map {
            it.setting_value
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "0"
        ).value
    }

    fun getInitialProteinLimit(): String {
        return settingsRepository.getByName(name = "protein").filterNotNull().map {
            it.setting_value
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "0"
        ).value
    }

    //init {
    //    viewModelScope.launch {}
    //}

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
