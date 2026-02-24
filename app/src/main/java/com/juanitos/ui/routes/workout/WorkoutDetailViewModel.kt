package com.juanitos.ui.routes.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.workout.entities.Workout
import com.juanitos.data.workout.entities.relations.WorkoutWithExercises
import com.juanitos.data.workout.repositories.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkoutDetailUiState(
    val workoutWithExercises: WorkoutWithExercises? = null,
)

class WorkoutDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val workoutId: Int = checkNotNull(savedStateHandle["workoutId"])

    val uiState: StateFlow<WorkoutDetailUiState> =
        workoutRepository.getByIdWithExercises(workoutId)
            .map { WorkoutDetailUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = WorkoutDetailUiState()
            )

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.delete(workout)
        }
    }

    companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}
