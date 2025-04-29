package com.juanitos.ui.food

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.Ingredient
import com.juanitos.data.IngredientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewFoodViewModel(
    private val ingredientsRepository: IngredientRepository,
) : ViewModel() {
    var searchExpanded by mutableStateOf(false)
        private set
    private val _ingredientQuery = MutableStateFlow("")
    val ingredientQuery: StateFlow<String> = _ingredientQuery.asStateFlow()

    var newIngredientOpen by mutableStateOf(false)
        private set
    var newIngredientUiState by mutableStateOf(NewIngredientUiState())
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
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000)
    )

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
}

data class NewIngredientUiState(
    val name: String = "",
    val kcal: String = "",
    val protein: String = "",
)