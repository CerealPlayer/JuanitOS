package com.juanitos.ui.routes.food.ingredients.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.repositories.IngredientRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngredientDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {
    private val ingredientId =
        savedStateHandle.get<String>("ingredientId")?.toIntOrNull()
            ?: throw IllegalArgumentException("ingredientId not found")

    val ingredient = ingredientRepository.getIngredientStream(ingredientId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    fun deleteIngredient(navigateUp: () -> Unit) {
        viewModelScope.launch {
            if (ingredient.value == null) {
                return@launch
            }
            ingredientRepository.deleteIngredient(ingredient.value!!)
            navigateUp()
        }
    }
}