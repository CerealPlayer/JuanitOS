package com.juanitos.ui.food

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.Ingredient
import com.juanitos.data.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewFoodViewModel(
    private val ingredientsRepository: IngredientRepository,
) : ViewModel() {
    var searchExpanded by mutableStateOf(false)
        private set
    var ingredientQuery by mutableStateOf("")
        private set
    var newIngredientOpen by mutableStateOf(false)
        private set

    var uiState: MutableStateFlow<NewFoodUiState> = MutableStateFlow(NewFoodUiState())
        private set

    fun onQueryChange(query: String) {
        if (query == "new_ingredient") {
            newIngredientOpen = true
            ingredientQuery = "New Ingredient"
            return
        }
        ingredientQuery = query
        searchIngredient(query)
    }

    fun onExpandedChange(expanded: Boolean) {
        searchExpanded = expanded
    }

    fun onIngredientOpenChange(open: Boolean) {
        newIngredientOpen = open
    }

    private fun searchIngredient(query: String) {
        viewModelScope.launch {
            ingredientsRepository.searchIngredientsStream(query).collect { ingredients ->
                uiState = MutableStateFlow(uiState.value.copy(ingredientSearch = ingredients))
            }
        }
    }

    fun onSearch(query: String) {
        if (query == "new_ingredient") {
            newIngredientOpen = true
            ingredientQuery = "New Ingredient"
            return
        }
        uiState = MutableStateFlow(
            uiState.value.copy(
                selectedIngredient = uiState.value.ingredientSearch.find { it.name == query }
            )
        )
    }
}

data class NewFoodUiState(
    val ingredientSearch: List<Ingredient> = listOf(),
    val selectedIngredient: Ingredient? = null,
)

