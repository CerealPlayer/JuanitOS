package com.juanitos.ui.routes.food.batch.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.entities.BatchFood
import com.juanitos.data.food.entities.BatchFoodIngredient
import com.juanitos.data.food.entities.Ingredient
import com.juanitos.data.food.entities.relations.IngredientDetail
import com.juanitos.data.food.repositories.BatchFoodIngredientRepository
import com.juanitos.data.food.repositories.BatchFoodRepository
import com.juanitos.data.food.repositories.IngredientRepository
import com.juanitos.lib.validateQtInt
import com.juanitos.ui.commons.food.IngredientEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditBatchFoodViewModel(
    savedStateHandle: SavedStateHandle,
    private val ingredientRepository: IngredientRepository,
    private val batchFoodRepository: BatchFoodRepository,
    private val batchFoodIngredientRepository: BatchFoodIngredientRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewBatchFoodUiState())
    private val batchFoodId: Int =
        savedStateHandle.get<Int>("batchFoodId") ?: throw IllegalArgumentException(
            "batch food id not found"
        )
    val uiState: StateFlow<NewBatchFoodUiState> = _uiState
        .combine(createIngredientsFlow()) { state, ingredients ->
            state.copy(ingredients = ingredients)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NewBatchFoodUiState()
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

    private fun createBatchFoodFlow(): Flow<BatchFood> {
        return batchFoodRepository.getBatchFood(batchFoodId).filterNotNull()
    }

    private fun createIngredientEntriesFlow(): Flow<List<IngredientEntry>> {
        return batchFoodRepository.getBatchFoodWithIngredients(batchFoodId).map {
            it?.ingredients?.map { ing -> ing.toIngredientEntry(it.id) } ?: emptyList()
        }.filterNotNull()
    }

    init {
        viewModelScope.launch {
            val batchFood = createBatchFoodFlow().first()
            val initialEntries = createIngredientEntriesFlow().first()
            _uiState.update {
                it.copy(
                    ingredientEntries = initialEntries,
                    batchFoodNameQuery = batchFood.name,
                    batchFoodTotalGramsQuery = batchFood.totalGrams.toString()
                )
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
        if (!validateQtInt(currentState.qtQuery)) return

        val ingredientEntry = IngredientEntry(
            ingredient = currentState.selectedIngredient,
            qt = currentState.qtQuery.toInt(),
            batchFoodId = batchFoodId,
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

    fun deleteIngredientEntry(entry: IngredientEntry) {
        _uiState.update {
            it.copy(
                ingredientEntries = it.ingredientEntries - entry,
                toDeleteEntries = it.toDeleteEntries + entry
            )
        }
    }

    fun dismissQtDialog() {
        _uiState.update { it.copy(ingredientQtDialogOpen = false) }
    }

    fun updateSaveDialogOpen(open: Boolean) {
        _uiState.update { it.copy(saveDialogOpen = open) }
    }

    fun updateBatchFoodName(query: String) {
        _uiState.update { it.copy(batchFoodNameQuery = query) }
    }

    fun updateBatchFoodTotalGrams(query: String) {
        _uiState.update { it.copy(batchFoodTotalGramsQuery = query) }
    }

    fun saveBatchFood(navigateUp: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.batchFoodNameQuery.isNotBlank() && validateQtInt(currentState.batchFoodTotalGramsQuery)) {
                batchFoodRepository.update(
                    BatchFood(
                        id = batchFoodId,
                        name = currentState.batchFoodNameQuery,
                        totalGrams = currentState.batchFoodTotalGramsQuery.toIntOrNull() ?: 0,
                    )
                )
                currentState.toDeleteEntries.forEach { entry ->
                    batchFoodIngredientRepository.delete(
                        entry.toBatchFoodIngredient()
                    )
                }
                currentState.ingredientEntries.forEach { entry ->
                    batchFoodIngredientRepository.upsert(
                        entry.toBatchFoodIngredient()
                    )
                }
                navigateUp()
            }
        }
    }
}

data class NewBatchFoodUiState(
    val searchQuery: String = "",
    val searchExpanded: Boolean = false,
    val ingredients: List<Ingredient> = emptyList(),
    val selectedIngredient: Ingredient? = null,
    val ingredientQtDialogOpen: Boolean = false,
    val qtQuery: String = "",
    val ingredientEntries: List<IngredientEntry> = emptyList(),
    val toDeleteEntries: List<IngredientEntry> = emptyList(),
    val saveDialogOpen: Boolean = false,
    val batchFoodNameQuery: String = "",
    val batchFoodTotalGramsQuery: String = ""
)

fun IngredientDetail.toIngredientEntry(batchFoodId: Int): IngredientEntry {
    return IngredientEntry(
        ingredient = Ingredient(
            id = this.ingredientId,
            name = this.name,
            proteinsPer100 = this.proteinsPer100,
            caloriesPer100 = this.caloriesPer100
        ),
        qt = this.grams,
        batchFoodId = batchFoodId,
        id = this.batchFoodIngredientId
    )
}

fun IngredientEntry.toBatchFoodIngredient(): BatchFoodIngredient {
    return BatchFoodIngredient(
        id = this.id ?: 0,
        batchFoodId = this.batchFoodId ?: 0,
        ingredientId = this.ingredient.id,
        grams = this.qt
    )
}