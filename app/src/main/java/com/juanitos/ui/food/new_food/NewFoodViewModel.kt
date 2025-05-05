package com.juanitos.ui.food.new_food

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.entities.FoodIngredient
import com.juanitos.data.food.entities.Ingredient
import com.juanitos.data.food.repositories.FoodIngredientRepository
import com.juanitos.data.food.repositories.FoodRepository
import com.juanitos.data.food.repositories.IngredientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewFoodViewModel(
    private val ingredientsRepository: IngredientRepository,
    private val foodRepository: FoodRepository,
    private val foodIngredientRepository: FoodIngredientRepository
) : ViewModel() {
    var searchExpanded by mutableStateOf(false)
        private set
    var saveFoodOpen by mutableStateOf(false)
        private set
    var newFoodName by mutableStateOf("")
        private set

    private val _ingredientQuery = MutableStateFlow("")
    val ingredientQuery: StateFlow<String> = _ingredientQuery.asStateFlow()

    var ingredientQt by mutableStateOf("")
        private set

    var newFoodUiState by mutableStateOf(NewFoodUiState())
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val ingredientSuggestions: StateFlow<List<Ingredient>> = _ingredientQuery.flatMapLatest {
        if (it.isNotBlank()) {
            ingredientsRepository.searchIngredientsStream(it).filterNotNull()
        } else {
            ingredientsRepository.getAllIngredientsStream()
        }
    }.stateIn(
        viewModelScope, initialValue = emptyList(), started = SharingStarted.WhileSubscribed(5_000)
    )

    val currentSelected: StateFlow<Ingredient?> = combine(
        _ingredientQuery, ingredientSuggestions
    ) { query, suggestions ->
        if (query.isNotEmpty()) {
            suggestions.firstOrNull { it.name.equals(query, ignoreCase = true) }
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null
    )

    fun onIngredientQtChange(qt: String) {
        ingredientQt = qt
    }

    fun onIngredientQtSave() {
        if (ingredientQt.isBlank()) {
            return
        }
        if (currentSelected.value == null) {
            return
        }
        newFoodUiState = newFoodUiState.copy(
            foodIngredients = newFoodUiState.foodIngredients + FoodIngredientDetails(
                ingredient = currentSelected.value!!, quantity = ingredientQt
            )
        )
        _ingredientQuery.value = ""
        ingredientQt = ""
    }

    fun onIngredientQtDismiss() {
        _ingredientQuery.value = ""
    }

    fun onQueryChange(query: String) {
        _ingredientQuery.value = query
    }

    fun onExpandedChange(expanded: Boolean) {
        searchExpanded = expanded
    }

    fun onSearch(query: String) {
        _ingredientQuery.value = query
    }

    fun onSaveFoodOpenChange(open: Boolean) {
        saveFoodOpen = open
    }

    fun onNewFoodNameChange(name: String) {
        newFoodName = name
    }

    fun onNewFoodSave(navigateUp: () -> Unit) {
        if (newFoodUiState.foodIngredients.isEmpty()) {
            return
        }
        viewModelScope.launch {
            val res = foodRepository.insertFood(
                newFoodName
            )
            val foodId = res.toInt()
            newFoodUiState.foodIngredients.forEach { foodIngredient ->
                foodIngredientRepository.insertFoodIngredient(
                    FoodIngredient(
                        foodId = foodId,
                        ingredientId = foodIngredient.ingredient.id,
                        grams = foodIngredient.quantity
                    )
                )
            }
            newFoodUiState = NewFoodUiState()
            newFoodName = ""
            saveFoodOpen = false
            navigateUp()
        }
    }
}

data class NewFoodUiState(
    val foodIngredients: List<FoodIngredientDetails> = emptyList(),
)

data class FoodIngredientDetails(
    val ingredient: Ingredient,
    val quantity: String,
)
