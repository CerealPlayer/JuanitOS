package com.juanitos.ui.routes.money.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.repositories.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewCategoryUiState(
    val nameInput: String = "",
    val isNameValid: Boolean = true,
    val descriptionInput: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

class NewCategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewCategoryUiState())
    val uiState: StateFlow<NewCategoryUiState> = _uiState.asStateFlow()

    fun setNameInput(input: String) {
        _uiState.value = _uiState.value.copy(
            nameInput = input,
            isNameValid = input.isNotBlank(),
            errorMessage = null
        )
    }

    fun setDescriptionInput(input: String) {
        _uiState.value = _uiState.value.copy(descriptionInput = input, errorMessage = null)
    }

    fun saveCategory(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        if (currentState.nameInput.isBlank()) {
            _uiState.value = currentState.copy(
                isNameValid = false,
                errorMessage = "Name cannot be empty"
            )
            return
        }

        _uiState.value = currentState.copy(isSaving = true)
        viewModelScope.launch {
            try {
                val description = currentState.descriptionInput.ifBlank { null }
                categoryRepository.insert(currentState.nameInput, description)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    success = true
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Error saving category"
                )
            }
        }
    }
}

