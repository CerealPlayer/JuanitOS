package com.juanitos.ui.routes.food.ingredients.new_ingredient

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.entities.Ingredient
import com.juanitos.data.food.repositories.IngredientRepository
import com.juanitos.lib.InputUiState
import com.juanitos.lib.validateQtDouble
import com.juanitos.lib.validateQtInt
import kotlinx.coroutines.launch

class NewIngredientViewModel(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {
    var uiState by mutableStateOf(NewIngredientUiState())
        private set

    fun updateName(name: String) {
        uiState = uiState.copy(
            name = InputUiState(
                value = name, touched = true, isValid = name.isNotBlank()
            )
        )
    }

    fun updateCalories(calories: String) {
        uiState = uiState.copy(
            calories = InputUiState(
                value = calories, touched = true, isValid = validateQtInt(calories)
            )
        )
    }

    fun updateProtein(protein: String) {
        uiState = uiState.copy(
            protein = InputUiState(
                value = protein, touched = true, isValid = validateQtDouble(protein)
            )
        )
    }

    fun saveIngredient(
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            if (uiState.name.isValid && uiState.calories.isValid && uiState.protein.isValid) {
                ingredientRepository.insertIngredient(
                    uiState.toIngredient()
                )
                onSuccess()
            }
        }
    }
}

data class NewIngredientUiState(
    val name: InputUiState = InputUiState(),
    val calories: InputUiState = InputUiState(),
    val protein: InputUiState = InputUiState(),
)

fun NewIngredientUiState.toIngredient(): Ingredient {
    return Ingredient(
        name = name.value,
        caloriesPer100 = calories.value.toIntOrNull() ?: 0,
        proteinsPer100 = protein.value.toDoubleOrNull() ?: 0.0
    )
}
