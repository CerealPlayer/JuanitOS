package com.juanitos.ui.routes.food.batch.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.repositories.BatchFoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BatchFoodDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val batchFoodRepository: BatchFoodRepository
) : ViewModel() {
    private val batchFoodId =
        savedStateHandle.get<Int>("batchFoodId")
            ?: throw IllegalArgumentException("batchFoodId not found")

    val batchFood = batchFoodRepository.getBatchFoodWithIngredients(batchFoodId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    fun deleteBatchFood(navigateUp: () -> Unit) {
        viewModelScope.launch {
            if (batchFood.value == null) return@launch
            batchFoodRepository.delete(batchFood.value!!.toBatchFood())
            navigateUp()
        }
    }
}
