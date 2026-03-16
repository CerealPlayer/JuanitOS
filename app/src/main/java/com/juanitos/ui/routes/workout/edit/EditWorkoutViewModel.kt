package com.juanitos.ui.routes.workout.edit

import android.database.sqlite.SQLiteException
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    val showDiscardDialog: Boolean = false,
    val hasChanges: Boolean = false,
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
    private val autosaveMutex = Mutex()
    private var isFinalizing = false
    private var isRestoring = false
    private var originalSnapshot: OriginalSnapshot? = null

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
            val loadedState = _state.value.copy(
                workout = workoutWithExercises?.workout,
                workoutId = workoutWithExercises?.workout?.id ?: workoutId,
                notesInput = workoutWithExercises?.workout?.notes.orEmpty(),
                exerciseGroups = groups,
                nextExerciseGroupId = maxGroupId + 1,
                nextSetId = maxSetId + 1,
                errorMessage = null,
            )
            originalSnapshot = buildSnapshot(loadedState)
            _state.update {
                loadedState.copy(hasChanges = false)
            }
        }
    }

    fun openDiscardDialog() {
        _state.update { it.copy(showDiscardDialog = true) }
    }

    fun closeDiscardDialog() {
        _state.update { it.copy(showDiscardDialog = false) }
    }

    fun discardChanges(onSuccess: () -> Unit) {
        val current = _state.value
        val currentWorkout = current.workout ?: run { onSuccess(); return }
        val snapshot = originalSnapshot ?: run { onSuccess(); return }
        viewModelScope.launch {
            isRestoring = true
            _state.update {
                it.copy(
                    isSavingSet = true,
                    showDiscardDialog = false,
                    errorMessage = null
                )
            }
            try {
                autosaveMutex.withLock {
                    restoreOriginalSnapshot(currentWorkout.id, snapshot)
                }
                val restoredState = _state.value.copy(
                    workout = snapshot.workout.copy(id = currentWorkout.id),
                    exerciseGroups = snapshot.exerciseGroups,
                    notesInput = snapshot.workout.notes.orEmpty(),
                    isSavingSet = false,
                    showDiscardDialog = false,
                    errorMessage = null,
                    hasChanges = false,
                )
                _state.value = restoredState
                onSuccess()
            } catch (e: SQLiteException) {
                _state.update {
                    it.copy(
                        isSavingSet = false,
                        errorMessage = e.message ?: "Error restoring workout"
                    )
                }
            } finally {
                isRestoring = false
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
        refreshHasChanges()
        queueAutosave()
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
        refreshHasChanges()
        queueAutosave()
    }

    fun deleteExercise(exerciseGroupId: Int) {
        _state.update { state ->
            state.copy(exerciseGroups = state.exerciseGroups.filterNot { it.id == exerciseGroupId })
        }
        refreshHasChanges()
        queueAutosave()
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
        refreshHasChanges()
    }

    fun saveWorkout(onSuccess: () -> Unit) {
        val current = uiState.value
        val currentWorkout = current.workout ?: run { onSuccess(); return }
        isFinalizing = true
        viewModelScope.launch {
            _state.update { it.copy(isSavingSet = true, errorMessage = null) }
            try {
                autosaveMutex.withLock {
                    val latest = _state.value
                    persistWorkoutStructure(currentWorkout.id, latest.exerciseGroups)
                    workoutRepository.update(
                        currentWorkout.copy(
                            notes = latest.notesInput.ifBlank { null },
                            endTime = LocalTime.now().format(TIME_FORMATTER)
                        )
                    )
                }
                val updatedWorkout = _state.value.workout?.copy(
                    notes = _state.value.notesInput.ifBlank { null },
                    endTime = LocalTime.now().format(TIME_FORMATTER)
                )
                _state.update { state ->
                    val stateWithWorkout = state.copy(
                        workout = updatedWorkout ?: state.workout,
                        isSavingSet = false,
                        hasChanges = false,
                    )
                    originalSnapshot = buildSnapshot(stateWithWorkout)
                    stateWithWorkout
                }
                onSuccess()
            } catch (e: SQLiteException) {
                _state.update {
                    it.copy(
                        isSavingSet = false,
                        errorMessage = e.message ?: "Error saving workout"
                    )
                }
            } finally {
                isFinalizing = false
            }
        }
    }

    private fun queueAutosave() {
        if (isFinalizing || isRestoring) return
        viewModelScope.launch {
            val currentWorkoutId = _state.value.workoutId ?: return@launch
            _state.update { it.copy(isSavingSet = true) }
            try {
                autosaveMutex.withLock {
                    if (isFinalizing || isRestoring) return@withLock
                    val latest = _state.value
                    persistWorkoutStructure(currentWorkoutId, latest.exerciseGroups)
                }
            } catch (e: SQLiteException) {
                _state.update { it.copy(errorMessage = e.message ?: "Error autosaving workout") }
            } finally {
                _state.update { it.copy(isSavingSet = false) }
            }
        }
    }

    private fun refreshHasChanges() {
        _state.update { state ->
            state.copy(hasChanges = computeHasChanges(state))
        }
    }

    private fun computeHasChanges(state: EditWorkoutUiState): Boolean {
        val snapshot = originalSnapshot ?: return false
        val normalizedNotes = state.notesInput.trim().ifBlank { null }
        val originalNotes = snapshot.workout.notes?.trim()?.ifBlank { null }
        if (normalizedNotes != originalNotes) return true
        return buildExerciseSnapshots(state.exerciseGroups) != buildExerciseSnapshots(snapshot.exerciseGroups)
    }

    private fun buildExerciseSnapshots(groups: List<ExerciseGroup>): List<ExerciseSnapshot> {
        return groups.map { group ->
            ExerciseSnapshot(
                exerciseDefinitionId = group.exercise.id,
                sets = group.sets.map { set ->
                    SetSnapshot(
                        reps = set.reps,
                        durationSeconds = set.durationSeconds,
                        weightKg = set.weightKg,
                    )
                }
            )
        }
    }

    private fun buildSnapshot(state: EditWorkoutUiState): OriginalSnapshot {
        val snapshotWorkout = state.workout ?: Workout(
            id = state.workoutId ?: workoutId,
            date = "",
            notes = state.notesInput.ifBlank { null }
        )
        return OriginalSnapshot(
            workout = snapshotWorkout.copy(notes = state.notesInput.ifBlank { null }),
            exerciseGroups = state.exerciseGroups
        )
    }

    private suspend fun persistWorkoutStructure(
        workoutId: Int,
        groups: List<ExerciseGroup>
    ) {
        val existingExercises = workoutExerciseRepository.getByWorkoutIdWithSets(workoutId).first()
        existingExercises.forEach { existing ->
            workoutExerciseRepository.delete(existing.workoutExercise)
        }
        groups.forEachIndexed { exerciseIndex, group ->
            val newWorkoutExerciseId = workoutExerciseRepository.insert(
                WorkoutExercise(
                    workoutId = workoutId,
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
    }

    private suspend fun restoreOriginalSnapshot(
        workoutId: Int,
        snapshot: OriginalSnapshot
    ) {
        persistWorkoutStructure(workoutId, snapshot.exerciseGroups)
        workoutRepository.update(snapshot.workout.copy(id = workoutId))
    }

    private data class OriginalSnapshot(
        val workout: Workout,
        val exerciseGroups: List<ExerciseGroup>,
    )

    private data class ExerciseSnapshot(
        val exerciseDefinitionId: Int,
        val sets: List<SetSnapshot>,
    )

    private data class SetSnapshot(
        val reps: Int?,
        val durationSeconds: Int?,
        val weightKg: Double?,
    )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
