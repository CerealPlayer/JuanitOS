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
import com.juanitos.lib.parseQtDouble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class SetDisplay(
    val id: Int,
    val setNumber: Int,
    val weightKg: Double?,
    val reps: Int?,
    val durationSeconds: Int?,
)

data class ExerciseGroup(
    val id: Int,
    val exercise: ExerciseDefinition,
    val sets: List<SetDisplay>,
)

data class NewWorkoutUiState(
    val date: String = "",
    val startTime: String? = null,
    val allExercises: List<ExerciseDefinition> = emptyList(),
    val exerciseGroups: List<ExerciseGroup> = emptyList(),
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
    val nextExerciseGroupId: Int = 1,
    val nextSetId: Int = 1,
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
            val startTime = LocalTime.now().format(TIME_FORMATTER)
            val workoutId = workoutRepository.insert(
                Workout(date = date, startTime = startTime)
            ).toInt()
            _state.update {
                it.copy(
                    workoutId = workoutId,
                    date = date,
                    startTime = startTime,
                    isReady = true
                )
            }
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
        val repsOrDuration = current.repsOrDurationInput.toIntOrNull()
        if (repsOrDuration == null || repsOrDuration <= 0) {
            val label = if (exercise.type == TYPE_REPS) "reps" else "duration"
            _state.update { it.copy(errorMessage = "Enter a valid $label value") }
            return
        }

        val weightKg = parseQtDouble(current.weightInput)?.takeIf { it > 0.0 }

        val reps = if (exercise.type == TYPE_REPS) repsOrDuration else null
        val duration = if (exercise.type == TYPE_DURATION) repsOrDuration else null
        _state.update { state ->
            val existingGroup = state.exerciseGroups.find { it.exercise.id == exercise.id }
            val setId = state.nextSetId
            val newSet = SetDisplay(
                id = setId,
                setNumber = (existingGroup?.sets?.size ?: 0) + 1,
                weightKg = weightKg,
                reps = reps,
                durationSeconds = duration,
            )
            val updatedGroups = if (existingGroup != null) {
                state.exerciseGroups.map { group ->
                    if (group.id == existingGroup.id) group.copy(sets = group.sets + newSet) else group
                }
            } else {
                state.exerciseGroups + ExerciseGroup(
                    id = state.nextExerciseGroupId,
                    exercise = exercise,
                    sets = listOf(newSet)
                )
            }
            state.copy(
                exerciseGroups = updatedGroups,
                nextSetId = setId + 1,
                nextExerciseGroupId = if (existingGroup == null) state.nextExerciseGroupId + 1 else state.nextExerciseGroupId,
                errorMessage = null,
            )
        }
    }

    fun deleteSet(exerciseGroupId: Int, setId: Int) {
        _state.update { state ->
            val updatedGroups = state.exerciseGroups.mapNotNull { group ->
                if (group.id != exerciseGroupId) return@mapNotNull group
                val remainingSets = group.sets.filterNot { it.id == setId }
                if (remainingSets.isEmpty()) {
                    null
                } else {
                    group.copy(
                        sets = remainingSets.mapIndexed { index, set ->
                            set.copy(setNumber = index + 1)
                        }
                    )
                }
            }
            state.copy(exerciseGroups = updatedGroups)
        }
    }

    fun deleteExercise(exerciseGroupId: Int) {
        _state.update { state ->
            state.copy(exerciseGroups = state.exerciseGroups.filterNot { it.id == exerciseGroupId })
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
        val endTime = LocalTime.now().format(TIME_FORMATTER)
        viewModelScope.launch {
            try {
                current.exerciseGroups.forEachIndexed { exerciseIndex, group ->
                    val workoutExerciseId = workoutExerciseRepository.insert(
                        WorkoutExercise(
                            workoutId = workoutId,
                            exerciseDefinitionId = group.exercise.id,
                            position = exerciseIndex
                        )
                    ).toInt()
                    group.sets.forEachIndexed { setIndex, set ->
                        workoutSetRepository.insert(
                            WorkoutSet(
                                workoutExerciseId = workoutExerciseId,
                                reps = set.reps,
                                durationSeconds = set.durationSeconds,
                                weightKg = set.weightKg,
                                position = setIndex
                            )
                        )
                    }
                }
                workoutRepository.update(
                    Workout(
                        id = workoutId,
                        date = current.date,
                        startTime = current.startTime,
                        endTime = endTime,
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
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
