package com.juanitos.ui.food

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.BatchFoodIngredient
import com.juanitos.data.food.BatchFoodIngredientRepository
import com.juanitos.data.food.BatchFoodRepository
import com.juanitos.data.food.Ingredient
import com.juanitos.data.food.IngredientRepository
import com.juanitos.lib.validateQt
import com.juanitos.ui.commons.food.IngredientEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class NewBatchFoodViewModel(
    private val ingredientRepository: IngredientRepository,
    private val batchFoodRepository: BatchFoodRepository,
    private val batchFoodIngredientRepository: BatchFoodIngredientRepository,
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
            selectedIngredient = uiState.ingredients.value.find { it.name == name },
            ingredientQtDialogOpen = true,
        )
    }

    fun updateQtQuery(query: String) {
        uiState = uiState.copy(
            qtQuery = query
        )
    }

    fun saveIngredientEntry() {
        if (uiState.selectedIngredient == null) return

        val ingredientEntry = IngredientEntry(
            ingredient = uiState.selectedIngredient!!, qt = uiState.qtQuery
        )
        uiState = uiState.copy(
            ingredientEntries = uiState.ingredientEntries + listOf(ingredientEntry),
            qtQuery = "",
            ingredientQtDialogOpen = false,
            selectedIngredient = null,
            searchQuery = ""
        )
    }

    fun dismissQtDialog() {
        uiState = uiState.copy(
            ingredientQtDialogOpen = false
        )
    }

    fun updateSaveDialogOpen(open: Boolean) {
        uiState = uiState.copy(
            saveDialogOpen = open,
        )
    }

    fun updateBatchFoodName(query: String) {
        uiState = uiState.copy(
            batchFoodNameQuery = query
        )
    }

    fun updateBatchFoodTotalGrams(query: String) {
        uiState = uiState.copy(
            batchFoodTotalGramsQuery = query
        )
    }

    fun saveBatchFood(navigateUp: () -> Unit) {
        viewModelScope.launch {
            if (uiState.batchFoodNameQuery.isNotBlank() && validateQt(uiState.batchFoodTotalGramsQuery)) {
                val id = batchFoodRepository.insert(
                    name = uiState.batchFoodNameQuery,
                    totalGrams = uiState.batchFoodTotalGramsQuery.toIntOrNull() ?: 0
                )
                val batchFoodId = id.toInt()
                uiState.ingredientEntries.forEach { entry ->
                    batchFoodIngredientRepository.insert(
                        BatchFoodIngredient(
                            batchFoodId = batchFoodId,
                            ingredientId = entry.ingredient.id,
                            grams = entry.qt
                        )
                    )
                }
                uiState = NewBatchFoodUiState()
                navigateUp()
            }
        }
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
    val selectedIngredient: Ingredient? = null,
    val ingredientQtDialogOpen: Boolean = false,
    val qtQuery: String = "",
    val ingredientEntries: List<IngredientEntry> = emptyList(),
    val saveDialogOpen: Boolean = false,
    val batchFoodNameQuery: String = "",
    val batchFoodTotalGramsQuery: String = ""
)