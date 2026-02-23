package com.juanitos.ui.routes.workout.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.workout.entities.ExerciseDefinition
import com.juanitos.data.workout.repositories.ExerciseDefinitionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewExerciseUiState(
    val nameInput: String = "",
    val isNameValid: Boolean = true,
    val descriptionInput: String = "",
    val typeInput: String = TYPE_REPS,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        const val TYPE_REPS = "reps"
        const val TYPE_DURATION = "duration"
    }
}

class NewExerciseViewModel(
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewExerciseUiState())
    val uiState: StateFlow<NewExerciseUiState> = _uiState.asStateFlow()

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

    fun setTypeInput(type: String) {
        _uiState.value = _uiState.value.copy(typeInput = type)
    }

    fun saveExercise(onSuccess: () -> Unit) {
        val current = _uiState.value

        if (current.nameInput.isBlank()) {
            _uiState.value =
                current.copy(isNameValid = false, errorMessage = "Name cannot be empty")
            return
        }

        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            try {
                exerciseDefinitionRepository.insert(
                    ExerciseDefinition(
                        name = current.nameInput.trim(),
                        description = current.descriptionInput.ifBlank { null },
                        type = current.typeInput
                    )
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Error saving exercise"
                )
            }
        }
    }
}
