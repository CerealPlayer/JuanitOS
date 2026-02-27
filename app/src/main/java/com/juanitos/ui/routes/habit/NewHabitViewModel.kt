package com.juanitos.ui.routes.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.habit.entities.Habit
import com.juanitos.data.habit.repositories.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewHabitUiState(
    val nameInput: String = "",
    val isNameValid: Boolean = true,
    val descriptionInput: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

class NewHabitViewModel(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewHabitUiState())
    val uiState: StateFlow<NewHabitUiState> = _uiState.asStateFlow()

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

    fun saveHabit(onSuccess: () -> Unit) {
        val current = _uiState.value

        if (current.nameInput.isBlank()) {
            _uiState.value =
                current.copy(isNameValid = false, errorMessage = "Name cannot be empty")
            return
        }

        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            try {
                habitRepository.insert(
                    Habit(
                        name = current.nameInput.trim(),
                        description = current.descriptionInput.ifBlank { null }
                    )
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Error saving habit"
                )
            }
        }
    }
}
