package com.juanitos.ui.routes.food

import android.icu.text.DecimalFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.repositories.FoodRepository
import com.juanitos.data.food.repositories.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FoodViewModel(
    private val settingsRepository: SettingsRepository,
    private val foodRepository: FoodRepository
) : ViewModel() {
    val calorieLimit = settingsRepository.getByName(name = "calorie").filterNotNull().map {
        it.settingValue
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = "0"
    )
    val proteinLimit = settingsRepository.getByName(name = "protein").filterNotNull().map {
        DecimalFormat("#.##").format(it.settingValue.toDoubleOrNull() ?: 0.0)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = "0.0"
    )
    val foods = foodRepository.getTodaysFoodsStream().map {
        it.map { foodDetails ->
            foodDetails.toFormattedFoodDetails()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}