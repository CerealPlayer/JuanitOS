package com.juanitos.ui.routes.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.repositories.FoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FoodViewModel(
    private val foodRepository: FoodRepository
) : ViewModel() {
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