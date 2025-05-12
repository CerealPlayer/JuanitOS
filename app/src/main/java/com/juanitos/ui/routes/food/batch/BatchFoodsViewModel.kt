package com.juanitos.ui.routes.food.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.repositories.BatchFoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class BatchFoodsViewModel(
    private val batchFoodRepository: BatchFoodRepository
) : ViewModel() {
    val batchFoods = batchFoodRepository.getBatchFoodsWithIngredients().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}