package com.juanitos.ui.routes.food.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.repositories.BatchFoodRepository
import com.juanitos.data.food.repositories.FoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val foodRepository: FoodRepository,
    private val batchFoodRepository: BatchFoodRepository
) : ViewModel() {
    private val foodId =
        savedStateHandle.get<Int>("foodId") ?: throw IllegalArgumentException("foodId not found")

    val food = foodRepository.getFoodDetailsStream(foodId).filterNotNull().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    fun deleteFood(navigateUp: () -> Unit) {
        viewModelScope.launch {
            val currentFood = food.value
            if (currentFood == null) {
                navigateUp()
                return@launch
            }
            currentFood.foodIngredients.forEach {
                val batchFood = it.batchFood
                if (batchFood != null) {
                    val usedGrams = it.foodIngredient.grams
                    batchFoodRepository.update(
                        batchFood.batchFood.copy(
                            totalGrams = batchFood.batchFood.totalGrams + usedGrams,
                        )
                    )
                }
            }
            foodRepository.deleteFood(currentFood.food)
            navigateUp()
        }
    }
}