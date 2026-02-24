package com.juanitos.ui.routes.workout.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.workout.entities.ExerciseDefinition
import com.juanitos.data.workout.repositories.ExerciseDefinitionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExercisesUiState(
    val exercises: List<ExerciseDefinition> = emptyList(),
)

class ExercisesViewModel(
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository
) : ViewModel() {
    val uiState: StateFlow<ExercisesUiState> = exerciseDefinitionRepository.getAll()
        .map { ExercisesUiState(exercises = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = ExercisesUiState()
        )

    fun deleteExercise(exercise: ExerciseDefinition) {
        viewModelScope.launch {
            exerciseDefinitionRepository.delete(exercise)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
