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
import com.juanitos.lib.parseQtDouble
import com.juanitos.ui.routes.workout.ExerciseGroup
import com.juanitos.ui.routes.workout.NewWorkoutViewModel
import com.juanitos.ui.routes.workout.SetDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    val nextExerciseGroupId: Int = 1,
    val nextSetId: Int = 1,
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
        exerciseDefinitionRepository.getAll()
    ) { state, allExercises ->
        val selectedExercise = state.selectedExercise?.let { selected ->
            allExercises.find { it.id == selected.id }
        } ?: allExercises.firstOrNull()

        state.copy(
            allExercises = allExercises,
            selectedExercise = selectedExercise,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = EditWorkoutUiState(workoutId = workoutId)
    )

    init {
        loadWorkout()
    }

    private fun loadWorkout() {
        viewModelScope.launch {
            val workoutWithExercises = workoutRepository.getByIdWithExercises(workoutId).first()
            val groups = workoutWithExercises?.exercises
                ?.sortedBy { it.workoutExercise.position }
                ?.map { exerciseWithSets ->
                    ExerciseGroup(
                        id = exerciseWithSets.workoutExercise.id,
                        exercise = exerciseWithSets.exerciseDefinition,
                        sets = exerciseWithSets.sets
                            .sortedBy { it.position }
                            .mapIndexed { index, set ->
                                SetDisplay(
                                    id = set.id,
                                    setNumber = index + 1,
                                    weightKg = set.weightKg,
                                    reps = set.reps,
                                    durationSeconds = set.durationSeconds
                                )
                            }
                    )
                }
                ?: emptyList()
            val maxGroupId = groups.maxOfOrNull { it.id } ?: 0
            val maxSetId = groups.flatMap { it.sets }.maxOfOrNull { it.id } ?: 0
            _state.update {
                it.copy(
                    workout = workoutWithExercises?.workout,
                    workoutId = workoutWithExercises?.workout?.id ?: workoutId,
                    notesInput = workoutWithExercises?.workout?.notes.orEmpty(),
                    exerciseGroups = groups,
                    nextExerciseGroupId = maxGroupId + 1,
                    nextSetId = maxSetId + 1
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
            val label = if (exercise.type == NewWorkoutViewModel.TYPE_REPS) "reps" else "duration"
            _state.update { it.copy(errorMessage = "Enter a valid $label value") }
            return
        }
        val weightKg = parseQtDouble(current.weightInput)?.takeIf { it > 0.0 }

        val reps = if (exercise.type == NewWorkoutViewModel.TYPE_REPS) repsOrDuration else null
        val duration =
            if (exercise.type == NewWorkoutViewModel.TYPE_DURATION) repsOrDuration else null
        _state.update { state ->
            val existingGroup = state.exerciseGroups.find { it.exercise.id == exercise.id }
            val setId = state.nextSetId
            val newSet = SetDisplay(
                id = setId,
                setNumber = (existingGroup?.sets?.size ?: 0) + 1,
                weightKg = weightKg,
                reps = reps,
                durationSeconds = duration
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
                errorMessage = null
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

    fun openSaveDialog() {
        _state.update {
            it.copy(
                showSaveDialog = true,
                notesInput = it.notesInput.ifBlank { uiState.value.workout?.notes.orEmpty() }
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
            val existingExercises =
                workoutExerciseRepository.getByWorkoutIdWithSets(currentWorkout.id).first()
            existingExercises.forEach { existing ->
                workoutExerciseRepository.delete(existing.workoutExercise)
            }
            current.exerciseGroups.forEachIndexed { exerciseIndex, group ->
                val newWorkoutExerciseId = workoutExerciseRepository.insert(
                    WorkoutExercise(
                        workoutId = currentWorkout.id,
                        exerciseDefinitionId = group.exercise.id,
                        position = exerciseIndex
                    )
                ).toInt()
                group.sets.forEachIndexed { setIndex, set ->
                    workoutSetRepository.insert(
                        WorkoutSet(
                            workoutExerciseId = newWorkoutExerciseId,
                            reps = set.reps,
                            durationSeconds = set.durationSeconds,
                            weightKg = set.weightKg,
                            position = setIndex
                        )
                    )
                }
            }
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
