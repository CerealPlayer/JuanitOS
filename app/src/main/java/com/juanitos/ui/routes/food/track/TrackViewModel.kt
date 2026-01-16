package com.juanitos.ui.routes.food.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.repositories.FoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TrackViewModel(
    private val foodRepository: FoodRepository
) : ViewModel() {
    val foods = foodRepository.getWeekFoodsStream().map {
        it.map { foodDetails ->
            foodDetails.toFormattedFoodDetails()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = emptyList()
    )
}
