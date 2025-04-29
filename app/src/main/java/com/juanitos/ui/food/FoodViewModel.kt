package com.juanitos.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FoodViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val calorieLimit = settingsRepository.getByName(name = "calorie").filterNotNull().map {
        it.settingValue
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = "0"
    )
    val proteinLimit = settingsRepository.getByName(name = "protein").filterNotNull().map {
        it.settingValue
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = "0"
    )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}