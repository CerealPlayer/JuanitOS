package com.juanitos.ui.routes.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.workout.entities.ExerciseDefinition
import com.juanitos.data.workout.entities.Workout
import com.juanitos.data.workout.entities.WorkoutExercise
import com.juanitos.data.workout.entities.WorkoutSet
import com.juanitos.data.workout.repositories.ExerciseDefinitionRepository
import com.juanitos.data.workout.repositories.WorkoutExerciseRepository
import com.juanitos.data.workout.repositories.WorkoutRepository
import com.juanitos.data.workout.repositories.WorkoutSetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SetDisplay(
    val setNumber: Int,
    val weightKg: Double?,
    val reps: Int?,
    val durationSeconds: Int?,
)

data class ExerciseGroup(
    val exercise: ExerciseDefinition,
    val workoutExerciseId: Int,
    val sets: List<SetDisplay>,
)

data class NewWorkoutUiState(
    val date: String = "",
    val allExercises: List<ExerciseDefinition> = emptyList(),
    val exerciseGroups: List<ExerciseGroup> = emptyList(),
    // map of exerciseDefinitionId -> workoutExerciseId for dedup
    val workoutExerciseIds: Map<Int, Int> = emptyMap(),
    val workoutId: Int? = null,
    val selectedExercise: ExerciseDefinition? = null,
    val weightInput: String = "0",
    val repsOrDurationInput: String = "",
    val isReady: Boolean = false,
    val isSavingSet: Boolean = false,
    val errorMessage: String? = null,
    // Save dialog
    val showSaveDialog: Boolean = false,
    val notesInput: String = "",
    // Discard dialog
    val showDiscardDialog: Boolean = false,
)

class NewWorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val workoutExerciseRepository: WorkoutExerciseRepository,
    private val workoutSetRepository: WorkoutSetRepository,
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NewWorkoutUiState())

    val uiState: StateFlow<NewWorkoutUiState> = combine(
        _state,
        exerciseDefinitionRepository.getAll()
    ) { state, exercises ->
        val updatedState = state.copy(allExercises = exercises)
        // Auto-select first exercise if none selected yet and exercises loaded
        if (updatedState.selectedExercise == null && exercises.isNotEmpty()) {
            updatedState.copy(selectedExercise = exercises.first())
        } else {
            updatedState
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = NewWorkoutUiState()
    )

    init {
        viewModelScope.launch {
            val date = LocalDate.now().toString()
            val workoutId = workoutRepository.insert(Workout(date = date)).toInt()
            _state.update { it.copy(workoutId = workoutId, date = date, isReady = true) }
        }
    }

    fun selectExercise(exercise: ExerciseDefinition) {
        _state.update { it.copy(selectedExercise = exercise, errorMessage = null) }
    }

    fun setWeightInput(value: String) {
        _state.update { it.copy(weightInput = value, errorMessage = null) }
    }

    fun setRepsOrDurationInput(value: String) {
        _state.update { it.copy(repsOrDurationInput = value, errorMessage = null) }
    }

    fun addSet() {
        val current = uiState.value
        val exercise = current.selectedExercise ?: return
        val workoutId = current.workoutId ?: return

        val repsOrDuration = current.repsOrDurationInput.toIntOrNull()
        if (repsOrDuration == null || repsOrDuration <= 0) {
            val label = if (exercise.type == TYPE_REPS) "reps" else "duration"
            _state.update { it.copy(errorMessage = "Enter a valid $label value") }
            return
        }

        val weightKg = current.weightInput.toDoubleOrNull()?.takeIf { it > 0.0 }

        _state.update { it.copy(isSavingSet = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val workoutExerciseId = current.workoutExerciseIds[exercise.id] ?: run {
                    val position = current.exerciseGroups.size
                    workoutExerciseRepository.insert(
                        WorkoutExercise(
                            workoutId = workoutId,
                            exerciseDefinitionId = exercise.id,
                            position = position
                        )
                    ).toInt()
                }

                val existingSets = current.exerciseGroups
                    .find { it.exercise.id == exercise.id }?.sets?.size ?: 0

                workoutSetRepository.insert(
                    WorkoutSet(
                        workoutExerciseId = workoutExerciseId,
                        reps = if (exercise.type == TYPE_REPS) repsOrDuration else null,
                        durationSeconds = if (exercise.type == TYPE_DURATION) repsOrDuration else null,
                        weightKg = weightKg,
                        position = existingSets
                    )
                )

                val newSet = SetDisplay(
                    setNumber = existingSets + 1,
                    weightKg = weightKg,
                    reps = if (exercise.type == TYPE_REPS) repsOrDuration else null,
                    durationSeconds = if (exercise.type == TYPE_DURATION) repsOrDuration else null,
                )

                _state.update { s ->
                    val newIds = s.workoutExerciseIds + (exercise.id to workoutExerciseId)
                    val existingGroup = s.exerciseGroups.find { it.exercise.id == exercise.id }
                    val newGroups = if (existingGroup != null) {
                        s.exerciseGroups.map { g ->
                            if (g.exercise.id == exercise.id) g.copy(sets = g.sets + newSet) else g
                        }
                    } else {
                        s.exerciseGroups + ExerciseGroup(
                            exercise = exercise,
                            workoutExerciseId = workoutExerciseId,
                            sets = listOf(newSet)
                        )
                    }
                    s.copy(
                        exerciseGroups = newGroups,
                        workoutExerciseIds = newIds,
                        isSavingSet = false,
                        // keep repsOrDurationInput for quick repeats, keep weightInput
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSavingSet = false,
                        errorMessage = e.message ?: "Error adding set"
                    )
                }
            }
        }
    }

    // ── Save dialog ──────────────────────────────────────────────────────────

    fun openSaveDialog() {
        _state.update { it.copy(showSaveDialog = true) }
    }

    fun closeSaveDialog() {
        _state.update { it.copy(showSaveDialog = false) }
    }

    fun setNotesInput(notes: String) {
        _state.update { it.copy(notesInput = notes) }
    }

    fun saveWorkout(onSuccess: () -> Unit) {
        val current = _state.value
        val workoutId = current.workoutId ?: run { onSuccess(); return }
        viewModelScope.launch {
            try {
                workoutRepository.update(
                    Workout(
                        id = workoutId,
                        date = current.date,
                        notes = current.notesInput.ifBlank { null }
                    )
                )
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message ?: "Error saving workout") }
            }
        }
    }

    // ── Discard dialog ───────────────────────────────────────────────────────

    fun openDiscardDialog() {
        _state.update { it.copy(showDiscardDialog = true) }
    }

    fun closeDiscardDialog() {
        _state.update { it.copy(showDiscardDialog = false) }
    }

    fun discardWorkout(onSuccess: () -> Unit) {
        val current = _state.value
        val workoutId = current.workoutId ?: run { onSuccess(); return }
        viewModelScope.launch {
            try {
                workoutRepository.delete(Workout(id = workoutId, date = current.date))
            } catch (_: Exception) {
                // best-effort delete; navigate up regardless
            }
            onSuccess()
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        const val TYPE_REPS = "reps"
        const val TYPE_DURATION = "duration"
    }
}
