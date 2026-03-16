package com.juanitos.ui.routes.climbing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.climbing.entities.ClimbingWorkout
import com.juanitos.data.climbing.repositories.ClimbingBoulderAttemptRepository
import com.juanitos.data.climbing.repositories.ClimbingWorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ClimbingWorkoutCardUiState(
    val id: Int,
    val date: String,
    val startTime: String?,
    val endTime: String?,
    val bouldersDoneCount: Int,
)

data class ClimbingUiState(
    val workouts: List<ClimbingWorkoutCardUiState> = emptyList(),
)

class ClimbingViewModel(
    private val climbingWorkoutRepository: ClimbingWorkoutRepository,
    climbingBoulderAttemptRepository: ClimbingBoulderAttemptRepository,
) : ViewModel() {
    val uiState: StateFlow<ClimbingUiState> = combine(
        climbingWorkoutRepository.getAll(),
        climbingBoulderAttemptRepository.getAll(),
    ) { workouts, attempts ->
        val bouldersCountByWorkoutId = attempts
            .groupBy { it.climbingWorkoutId }
            .mapValues { (_, workoutAttempts) ->
                workoutAttempts.map { it.climbingBoulderId }.distinct().count()
            }

        ClimbingUiState(
            workouts = workouts.map { workout ->
                ClimbingWorkoutCardUiState(
                    id = workout.id,
                    date = workout.date,
                    startTime = workout.startTime,
                    endTime = workout.endTime,
                    bouldersDoneCount = bouldersCountByWorkoutId[workout.id] ?: 0,
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = ClimbingUiState(),
    )

    fun deleteWorkout(workout: ClimbingWorkoutCardUiState) {
        viewModelScope.launch {
            climbingWorkoutRepository.delete(
                ClimbingWorkout(
                    id = workout.id,
                    date = workout.date,
                    startTime = workout.startTime,
                    endTime = workout.endTime,
                )
            )
        }
    }

    private companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
