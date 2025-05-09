package com.juanitos.ui.routes.food.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.entities.Ingredient
import com.juanitos.data.food.repositories.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngredientsViewModel(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(IngredientUiState())
    val uiState = _uiState
        .combine(createIngredientsFlow()) { state, ingredients ->
            state.copy(ingredients = ingredients)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = IngredientUiState()
        )

    private fun createIngredientsFlow(): Flow<List<Ingredient>> {
        return ingredientRepository.getAllIngredientsStream()
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientRepository.deleteIngredient(ingredient)
        }
    }
}

data class IngredientUiState(
    val ingredients: List<Ingredient> = emptyList(),
)