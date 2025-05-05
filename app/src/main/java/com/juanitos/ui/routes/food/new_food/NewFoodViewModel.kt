package com.juanitos.ui.routes.food.new_food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.entities.Ingredient
import com.juanitos.data.food.repositories.BatchFoodRepository
import com.juanitos.data.food.repositories.IngredientRepository
import com.juanitos.ui.commons.food.IngredientEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewFoodViewModel(
    private val ingredientRepository: IngredientRepository,
    private val batchFoodRepository: BatchFoodRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewFoodUiState())
    val uiState: StateFlow<NewFoodUiState> = _uiState
        .combine(createIngredientsFlow()) { state, ingredients ->
            state.copy(ingredients = ingredients)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NewFoodUiState()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createIngredientsFlow(): Flow<List<Ingredient>> {
        return _uiState
            .map { it.searchQuery }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isEmpty()) {
                    ingredientRepository.getAllIngredientsStream()
                } else {
                    ingredientRepository.searchIngredientsStream(query)
                }
            }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateSearchExpanded(value: Boolean) {
        _uiState.update { it.copy(searchExpanded = value) }
    }

    fun selectIngredient(name: String) {
        _uiState.update {
            it.copy(
                selectedIngredient = uiState.value.ingredients.find { ingredient -> ingredient.name == name },
                ingredientQtDialogOpen = true
            )
        }
    }

    fun updateQtQuery(query: String) {
        _uiState.update { it.copy(qtQuery = query) }
    }

    fun saveIngredientEntry() {
        val currentState = _uiState.value
        if (currentState.selectedIngredient == null) return

        val ingredientEntry = IngredientEntry(
            ingredient = currentState.selectedIngredient,
            qt = currentState.qtQuery
        )

        _uiState.update {
            it.copy(
                ingredientEntries = it.ingredientEntries + ingredientEntry,
                qtQuery = "",
                ingredientQtDialogOpen = false,
                selectedIngredient = null,
                searchQuery = ""
            )
        }
    }

    fun dismissQtDialog() {
        _uiState.update { it.copy(ingredientQtDialogOpen = false) }
    }

    fun updateSaveDialogOpen(open: Boolean) {
        _uiState.update { it.copy(saveDialogOpen = open) }
    }

    fun updateFoodName(query: String) {
        _uiState.update { it.copy(batchFoodNameQuery = query) }
    }

    fun saveFood(navigateUp: () -> Unit) {
        viewModelScope.launch {

        }
    }
}

data class NewFoodUiState(
    val searchQuery: String = "",
    val searchExpanded: Boolean = false,
    val ingredients: List<Ingredient> = emptyList(),
    val selectedIngredient: Ingredient? = null,
    val ingredientQtDialogOpen: Boolean = false,
    val qtQuery: String = "",
    val ingredientEntries: List<IngredientEntry> = emptyList(),
    val saveDialogOpen: Boolean = false,
    val batchFoodNameQuery: String = "",
)