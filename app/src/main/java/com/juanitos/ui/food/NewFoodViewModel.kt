package com.juanitos.ui.food

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.Food
import com.juanitos.data.FoodIngredient
import com.juanitos.data.FoodIngredientRepository
import com.juanitos.data.FoodRepository
import com.juanitos.data.Ingredient
import com.juanitos.data.IngredientRepository
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

    var newIngredientOpen by mutableStateOf(false)
        private set
    var newIngredientUiState by mutableStateOf(NewIngredientUiState())
        private set
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
        viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val currentSelected: StateFlow<Ingredient?> = combine(
        _ingredientQuery,
        ingredientSuggestions
    ) { query, suggestions ->
        if (query.isNotEmpty()) {
            suggestions.firstOrNull { it.name.equals(query, ignoreCase = true) }
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
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
                ingredient = currentSelected.value!!,
                quantity = ingredientQt
            )
        )
        _ingredientQuery.value = ""
        ingredientQt = ""
    }

    fun onIngredientQtDismiss() {
        _ingredientQuery.value = ""
    }

    fun onNewIngredientChange(
        name: String,
        kcal: String,
        protein: String,
    ) {
        newIngredientUiState = newIngredientUiState.copy(
            name = name,
            kcal = kcal,
            protein = protein
        )
    }

    private fun validateNewIngredient(): Boolean {
        if (newIngredientUiState.name.isNotBlank() &&
            newIngredientUiState.kcal.isNotBlank() &&
            newIngredientUiState.protein.isNotBlank()
        ) {
            return true
        }
        return false
    }

    fun onNewIngredientSave() {
        if (!validateNewIngredient()) {
            return
        }
        viewModelScope.launch {
            ingredientsRepository.insertIngredient(
                Ingredient(
                    name = newIngredientUiState.name,
                    caloriesPer100 = newIngredientUiState.kcal,
                    proteinsPer100 = newIngredientUiState.protein
                )
            )
            newIngredientUiState = NewIngredientUiState()
        }
        newIngredientOpen = false
        _ingredientQuery.value = ""
    }

    fun onQueryChange(query: String) {
        if (query == "new_ingredient") {
            newIngredientOpen = true
            _ingredientQuery.value = "New Ingredient"
            return
        }
        _ingredientQuery.value = query
    }

    fun onExpandedChange(expanded: Boolean) {
        searchExpanded = expanded
    }

    fun onNewIngredientClose() {
        newIngredientOpen = false
        _ingredientQuery.value = ""
    }

    fun onSearch(query: String) {
        if (query == "new_ingredient") {
            newIngredientOpen = true
            _ingredientQuery.value = "New Ingredient"
            return
        }
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
                Food(
                    name = newFoodName,
                )
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

data class NewIngredientUiState(
    val name: String = "",
    val kcal: String = "",
    val protein: String = "",
)