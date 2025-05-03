package com.juanitos.ui.food

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.Ingredient
import com.juanitos.data.food.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class NewBatchFoodViewModel(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {
    var uiState by mutableStateOf(NewBatchFoodUiState())
        private set

    fun updateSearchQuery(query: String) {
        uiState = uiState.copy(
            searchQuery = query
        )
        fetchIngredients(query)
    }

    fun updateSearchExpanded(value: Boolean) {
        uiState = uiState.copy(
            searchExpanded = value
        )
    }

    fun selectIngredient(name: String) {
        uiState = uiState.copy(
            selectedIngredient = uiState.ingredients.value.find { it.name == name }
        )
    }

    private fun fetchIngredients(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                ingredientRepository.getAllIngredientsStream().filterNotNull()
                    .collect { uiState.ingredients.value = it }

            } else {
                ingredientRepository.searchIngredientsStream(query).filterNotNull()
                    .collect { uiState.ingredients.value = it }
            }
        }
    }

    init {
        fetchIngredients("")
    }
}

data class NewBatchFoodUiState(
    val searchQuery: String = "",
    val searchExpanded: Boolean = false,
    val ingredients: MutableStateFlow<List<Ingredient>> = MutableStateFlow(emptyList()),
    val selectedIngredient: Ingredient? = null
)
