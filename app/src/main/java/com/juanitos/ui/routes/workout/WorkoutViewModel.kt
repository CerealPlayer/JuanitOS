package com.juanitos.ui.routes.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.workout.entities.Workout
import com.juanitos.data.workout.repositories.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkoutUiState(
    val workouts: List<Workout> = emptyList(),
)

class WorkoutViewModel(private val workoutRepository: WorkoutRepository) : ViewModel() {
    val uiState: StateFlow<WorkoutUiState> = workoutRepository.getAll()
        .map { WorkoutUiState(workouts = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = WorkoutUiState()
        )

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.delete(workout)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
