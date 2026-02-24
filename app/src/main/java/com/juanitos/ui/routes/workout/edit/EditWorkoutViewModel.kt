package com.juanitos.ui.routes.workout.edit

import androidx.lifecycle.SavedStateHandle
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
import com.juanitos.ui.routes.workout.ExerciseGroup
import com.juanitos.ui.routes.workout.NewWorkoutViewModel
import com.juanitos.ui.routes.workout.SetDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class EditWorkoutUiState(
    val workout: Workout? = null,
    val allExercises: List<ExerciseDefinition> = emptyList(),
    val exerciseGroups: List<ExerciseGroup> = emptyList(),
    val workoutId: Int? = null,
    val selectedExercise: ExerciseDefinition? = null,
    val weightInput: String = "0",
    val repsOrDurationInput: String = "",
    val isSavingSet: Boolean = false,
    val errorMessage: String? = null,
    val showSaveDialog: Boolean = false,
    val notesInput: String = "",
)

class EditWorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val workoutExerciseRepository: WorkoutExerciseRepository,
    private val workoutSetRepository: WorkoutSetRepository,
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository,
) : ViewModel() {

    private val workoutId: Int = checkNotNull(savedStateHandle["workoutId"])
    private val _state = MutableStateFlow(EditWorkoutUiState(workoutId = workoutId))

    val uiState: StateFlow<EditWorkoutUiState> = combine(
        _state,
        workoutRepository.getByIdWithExercises(workoutId),
        exerciseDefinitionRepository.getAll()
    ) { state, workoutWithExercises, allExercises ->
        val groups = workoutWithExercises?.exercises
            ?.sortedBy { it.workoutExercise.position }
            ?.map { exerciseWithSets ->
                ExerciseGroup(
                    exercise = exerciseWithSets.exerciseDefinition,
                    workoutExerciseId = exerciseWithSets.workoutExercise.id,
                    sets = exerciseWithSets.sets
                        .sortedBy { it.position }
                        .mapIndexed { index, set ->
                            SetDisplay(
                                setNumber = index + 1,
                                weightKg = set.weightKg,
                                reps = set.reps,
                                durationSeconds = set.durationSeconds
                            )
                        }
                )
            }
            ?: emptyList()
        val selectedExercise = state.selectedExercise?.let { selected ->
            allExercises.find { it.id == selected.id }
        } ?: allExercises.firstOrNull()

        state.copy(
            workout = workoutWithExercises?.workout,
            workoutId = workoutWithExercises?.workout?.id ?: state.workoutId,
            allExercises = allExercises,
            exerciseGroups = groups,
            selectedExercise = selectedExercise,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = EditWorkoutUiState(workoutId = workoutId)
    )

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
        val currentWorkoutId = current.workoutId ?: return
        val repsOrDuration = current.repsOrDurationInput.toIntOrNull()
        if (repsOrDuration == null || repsOrDuration <= 0) {
            val label = if (exercise.type == NewWorkoutViewModel.TYPE_REPS) "reps" else "duration"
            _state.update { it.copy(errorMessage = "Enter a valid $label value") }
            return
        }
        val weightKg = current.weightInput.toDoubleOrNull()?.takeIf { it > 0.0 }

        _state.update { it.copy(isSavingSet = true, errorMessage = null) }
        viewModelScope.launch {
            val existingGroup = current.exerciseGroups.find { it.exercise.id == exercise.id }
            val workoutExerciseId =
                existingGroup?.workoutExerciseId ?: workoutExerciseRepository.insert(
                    WorkoutExercise(
                        workoutId = currentWorkoutId,
                        exerciseDefinitionId = exercise.id,
                        position = current.exerciseGroups.size
                    )
                ).toInt()
            val existingSets = existingGroup?.sets?.size ?: 0
            workoutSetRepository.insert(
                WorkoutSet(
                    workoutExerciseId = workoutExerciseId,
                    reps = if (exercise.type == NewWorkoutViewModel.TYPE_REPS) repsOrDuration else null,
                    durationSeconds = if (exercise.type == NewWorkoutViewModel.TYPE_DURATION) repsOrDuration else null,
                    weightKg = weightKg,
                    position = existingSets
                )
            )
            _state.update { it.copy(isSavingSet = false) }
        }
    }

    fun openSaveDialog() {
        _state.update {
            it.copy(
                showSaveDialog = true,
                notesInput = if (it.notesInput.isBlank()) uiState.value.workout?.notes.orEmpty() else it.notesInput
            )
        }
    }

    fun closeSaveDialog() {
        _state.update { it.copy(showSaveDialog = false) }
    }

    fun setNotesInput(notes: String) {
        _state.update { it.copy(notesInput = notes) }
    }

    fun saveWorkout(onSuccess: () -> Unit) {
        val current = uiState.value
        val currentWorkout = current.workout ?: run { onSuccess(); return }
        viewModelScope.launch {
            workoutRepository.update(
                currentWorkout.copy(
                    notes = current.notesInput.ifBlank { null },
                    endTime = LocalTime.now().format(TIME_FORMATTER)
                )
            )
            onSuccess()
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
