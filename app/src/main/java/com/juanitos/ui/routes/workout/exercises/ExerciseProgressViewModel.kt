package com.juanitos.ui.routes.workout.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.workout.entities.ExerciseDefinition
import com.juanitos.data.workout.repositories.ExerciseDefinitionRepository
import com.juanitos.data.workout.repositories.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.abs

data class ExerciseProgressPoint(
    val workoutDate: String,
    val workoutId: Int,
    val weightKg: Double?,
    val sets: Int,
)

data class ExerciseProgressUiState(
    val exercise: ExerciseDefinition? = null,
    val points: List<ExerciseProgressPoint> = emptyList(),
) {
    val hasWorkouts: Boolean
        get() = points.isNotEmpty()
}

class ExerciseProgressViewModel(
    savedStateHandle: SavedStateHandle,
    exerciseDefinitionRepository: ExerciseDefinitionRepository,
    workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val exerciseId: Int = checkNotNull(savedStateHandle["exerciseId"])

    val uiState: StateFlow<ExerciseProgressUiState> = combine(
        exerciseDefinitionRepository.getById(exerciseId),
        workoutRepository.getAllWithExercises(),
    ) { exercise, workouts ->
        val points = workouts
            .sortedBy { it.workout.date }
            .mapNotNull { workout ->
                val sets = workout.exercises
                    .filter { it.workoutExercise.exerciseDefinitionId == exerciseId }
                    .flatMap { it.sets }

                if (sets.isEmpty()) {
                    null
                } else {
                    val maxWeight = sets.mapNotNull { it.weightKg }.maxOrNull()
                    val setsAtMaxWeight = if (maxWeight == null) {
                        sets.filter { it.weightKg == null }
                    } else {
                        sets.filter { set ->
                            set.weightKg != null && abs(set.weightKg - maxWeight) < 0.0001
                        }
                    }
                    ExerciseProgressPoint(
                        workoutDate = workout.workout.date,
                        workoutId = workout.workout.id,
                        weightKg = maxWeight,
                        sets = setsAtMaxWeight.size,
                    )
                }
            }

        ExerciseProgressUiState(
            exercise = exercise,
            points = points,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = ExerciseProgressUiState(),
    )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

