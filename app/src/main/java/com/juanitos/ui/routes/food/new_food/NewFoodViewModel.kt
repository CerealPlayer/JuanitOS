package com.juanitos.ui.routes.food.new_food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.entities.FoodIngredient
import com.juanitos.data.food.entities.Ingredient
import com.juanitos.data.food.entities.relations.BatchFoodWithIngredientDetails
import com.juanitos.data.food.repositories.BatchFoodRepository
import com.juanitos.data.food.repositories.FoodIngredientRepository
import com.juanitos.data.food.repositories.FoodRepository
import com.juanitos.data.food.repositories.IngredientRepository
import com.juanitos.lib.validateQtInt
import com.juanitos.ui.commons.food.BatchFoodEntry
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
    private val foodRepository: FoodRepository,
    private val foodIngredientRepository: FoodIngredientRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewFoodUiState())
    val uiState: StateFlow<NewFoodUiState> = _uiState
        .combine(createIngredientsFlow()) { state, ingredients ->
            state.copy(ingredients = ingredients)
        }
        .combine(createBatchFoodsFlow()) { state, batchFoods ->
            state.copy(batchFoods = batchFoods)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createBatchFoodsFlow(): Flow<List<BatchFoodWithIngredientDetails>> {
        return _uiState.map { it.searchQuery }.distinctUntilChanged().flatMapLatest { query ->
            if (query.isEmpty()) {
                batchFoodRepository.getBatchFoodsWithIngredients()
            } else {
                batchFoodRepository.searchBatchFoods(query)
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

    fun selectBatchFood(name: String) {
        _uiState.update {
            it.copy(
                selectedBatchFood = uiState.value.batchFoods.find { batchFood -> batchFood.name == name },
                batchFoodQtDialogOpen = true
            )
        }
    }

    fun updateQtQuery(query: String) {
        _uiState.update { it.copy(qtQuery = query) }
    }

    fun saveIngredientEntry() {
        val currentState = _uiState.value
        if (!validateQtInt(currentState.qtQuery)) return
        if (currentState.selectedIngredient == null) return

        val ingredientEntry = IngredientEntry(
            ingredient = currentState.selectedIngredient,
            qt = currentState.qtQuery.toInt()
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

    fun saveBatchFoodEntry() {
        val currentState = _uiState.value
        if (!validateQtInt(currentState.qtQuery)) return
        if (currentState.selectedBatchFood == null) return
        if (!validateQtInt(currentState.qtQuery)) return

        val batchFoodEntry = BatchFoodEntry(
            batchFood = currentState.selectedBatchFood,
            qt = currentState.qtQuery.toInt()
        )

        _uiState.update {
            it.copy(
                batchFoodEntries = it.batchFoodEntries + batchFoodEntry,
                qtQuery = "",
                batchFoodQtDialogOpen = false,
                selectedBatchFood = null,
                searchQuery = ""
            )
        }
    }

    fun dismissQtDialog() {
        _uiState.update { it.copy(ingredientQtDialogOpen = false) }
    }

    fun dismissBatchFoodQtDialog() {
        _uiState.update { it.copy(batchFoodQtDialogOpen = false) }
    }

    fun updateSaveDialogOpen(open: Boolean) {
        _uiState.update { it.copy(saveDialogOpen = open) }
    }

    fun updateFoodName(query: String) {
        _uiState.update { it.copy(foodNameQuery = query) }
    }

    fun saveFood(navigateUp: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.ingredientEntries.isEmpty() && currentState.batchFoodEntries.isEmpty()) return@launch
            if (currentState.foodNameQuery.isEmpty()) return@launch

            val foodId = foodRepository.insertFood(currentState.foodNameQuery)

            currentState.ingredientEntries.forEach { entry ->
                if (!validateQtInt(entry.qt.toString())) return@forEach
                foodIngredientRepository.insertFoodIngredient(
                    FoodIngredient(
                        foodId = foodId.toInt(),
                        ingredientId = entry.ingredient.id,
                        grams = entry.qt,
                        batchFoodId = null
                    )
                )
            }

            currentState.batchFoodEntries.forEach { entry ->
                foodIngredientRepository.insertFoodIngredient(
                    FoodIngredient(
                        foodId = foodId.toInt(),
                        ingredientId = null,
                        grams = entry.qt,
                        batchFoodId = entry.batchFood.id
                    )
                )
                batchFoodRepository.update(
                    entry.batchFood.toBatchFood().copy(
                        gramsUsed = entry.batchFood.gramsUsed?.plus(entry.qt)
                            ?: entry.qt
                    )
                )
            }

            _uiState.update { it.copy(saveDialogOpen = false) }
            navigateUp()
        }
    }
}

data class NewFoodUiState(
    val searchQuery: String = "",
    val searchExpanded: Boolean = false,
    val ingredients: List<Ingredient> = emptyList(),
    val batchFoods: List<BatchFoodWithIngredientDetails> = emptyList(),
    val selectedIngredient: Ingredient? = null,
    val selectedBatchFood: BatchFoodWithIngredientDetails? = null,
    val ingredientQtDialogOpen: Boolean = false,
    val batchFoodQtDialogOpen: Boolean = false,
    val qtQuery: String = "",
    val ingredientEntries: List<IngredientEntry> = emptyList(),
    val batchFoodEntries: List<BatchFoodEntry> = emptyList(),
    val saveDialogOpen: Boolean = false,
    val foodNameQuery: String = "",
)