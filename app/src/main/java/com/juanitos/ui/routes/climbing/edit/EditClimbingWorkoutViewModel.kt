package com.juanitos.ui.routes.climbing.edit

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.climbing.entities.ClimbingBoulder
import com.juanitos.data.climbing.entities.ClimbingBoulderAttempt
import com.juanitos.data.climbing.entities.ClimbingMedia
import com.juanitos.data.climbing.entities.ClimbingWorkout
import com.juanitos.data.climbing.repositories.ClimbingBoulderAttemptRepository
import com.juanitos.data.climbing.repositories.ClimbingBoulderRepository
import com.juanitos.data.climbing.repositories.ClimbingMediaRepository
import com.juanitos.data.climbing.repositories.ClimbingWorkoutRepository
import com.juanitos.ui.routes.climbing.BoulderSelectionUiState
import com.juanitos.ui.routes.climbing.ClimbingAttemptUiState
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
import java.io.File
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class EditClimbingWorkoutUiState(
    val workoutId: Int? = null,
    val date: String = "",
    val startTime: String? = null,
    val endTime: String? = null,
    val notes: String = "",
    val showBoulderDialog: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val boulders: List<BoulderSelectionUiState> = emptyList(),
    val selectedBoulderId: Int? = null,
    val selectedBoulderIds: List<Int> = emptyList(),
    val attemptsByBoulderId: Map<Int, List<ClimbingAttemptUiState>> = emptyMap(),
    val nextAttemptId: Int = 1,
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false,
    val hasChanges: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedBoulder: BoulderSelectionUiState?
        get() = boulders.firstOrNull { it.id == selectedBoulderId }

    val totalAttemptsCount: Int
        get() = attemptsByBoulderId.values.sumOf { it.size }

    val canSave: Boolean
        get() = workoutId != null && totalAttemptsCount > 0 && !isSaving && isLoaded
}

class EditClimbingWorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val climbingWorkoutRepository: ClimbingWorkoutRepository,
    private val climbingBoulderAttemptRepository: ClimbingBoulderAttemptRepository,
    climbingBoulderRepository: ClimbingBoulderRepository,
    private val climbingMediaRepository: ClimbingMediaRepository,
    private val appContext: Context,
) : ViewModel() {
    private val workoutId: Int = checkNotNull(savedStateHandle["workoutId"])
    private val _uiState = MutableStateFlow(EditClimbingWorkoutUiState(workoutId = workoutId))
    private val autosaveMutex = Mutex()
    private var isFinalizing = false
    private var isRestoring = false
    private var originalSnapshot: OriginalSnapshot? = null

    val uiState: StateFlow<EditClimbingWorkoutUiState> =
        combine(
            _uiState,
            climbingBoulderRepository.getAll(),
            climbingMediaRepository.getAll(),
        ) { state, boulders, media ->
            state.copy(boulders = buildBoulderUiState(boulders, media))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = EditClimbingWorkoutUiState(workoutId = workoutId),
        )

    init {
        loadWorkoutForEdit()
    }

    fun setNotes(input: String) {
        _uiState.update { state ->
            val updated = state.copy(notes = input, errorMessage = null)
            updated.copy(hasChanges = computeHasChanges(updated))
        }
        queueAutosave()
    }

    fun openBoulderDialog() {
        _uiState.update { it.copy(showBoulderDialog = true) }
    }

    fun closeBoulderDialog() {
        _uiState.update { it.copy(showBoulderDialog = false) }
    }

    fun openDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = true) }
    }

    fun closeDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }

    fun discardChanges(onSuccess: () -> Unit) {
        val current = _uiState.value
        val currentWorkoutId = current.workoutId ?: run { onSuccess(); return }
        val snapshot = originalSnapshot ?: run { onSuccess(); return }
        viewModelScope.launch {
            isRestoring = true
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    showDiscardDialog = false
                )
            }
            try {
                autosaveMutex.withLock {
                    restoreOriginalSnapshot(currentWorkoutId, snapshot)
                }
                _uiState.update {
                    it.copy(
                        date = snapshot.workout.date,
                        startTime = snapshot.workout.startTime,
                        endTime = snapshot.workout.endTime,
                        notes = snapshot.workout.notes.orEmpty(),
                        selectedBoulderId = snapshot.selectedBoulderIds.firstOrNull(),
                        selectedBoulderIds = snapshot.selectedBoulderIds,
                        attemptsByBoulderId = snapshot.attemptsByBoulderId,
                        nextAttemptId = snapshot.nextAttemptId,
                        isSaving = false,
                        hasChanges = false,
                        errorMessage = null,
                    )
                }
                onSuccess()
            } catch (e: SQLiteException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Error restoring workout"
                    )
                }
            } finally {
                isRestoring = false
            }
        }
    }

    fun selectBoulder(boulderId: Int) {
        _uiState.update { state ->
            val updated = state.copy(
                selectedBoulderId = boulderId,
                selectedBoulderIds = if (state.selectedBoulderIds.contains(boulderId)) {
                    state.selectedBoulderIds
                } else {
                    state.selectedBoulderIds + boulderId
                },
                showBoulderDialog = false,
                errorMessage = null,
            )
            updated.copy(hasChanges = computeHasChanges(updated))
        }
        queueAutosave()
    }

    fun addAttemptFromUri(videoUri: Uri) {
        val current = _uiState.value
        val selectedBoulderId = current.selectedBoulderId ?: run {
            _uiState.update { it.copy(errorMessage = "Select a boulder before adding an attempt") }
            return
        }
        val currentWorkoutId = current.workoutId ?: run {
            _uiState.update { it.copy(errorMessage = "Workout is not ready yet") }
            return
        }
        val attemptId = current.nextAttemptId
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val savedMedia = saveVideoMedia(videoUri = videoUri, attemptId = attemptId)
                _uiState.update { state ->
                    val latestAttempts = state.attemptsByBoulderId[selectedBoulderId].orEmpty()
                    val updated = state.copy(
                        attemptsByBoulderId = state.attemptsByBoulderId + (
                                selectedBoulderId to (
                                        latestAttempts + ClimbingAttemptUiState(
                                            id = attemptId,
                                            videoMediaId = savedMedia.id,
                                            videoPath = savedMedia.filePath,
                                        )
                                        )
                                ),
                        nextAttemptId = attemptId + 1,
                        errorMessage = null,
                    )
                    updated.copy(hasChanges = computeHasChanges(updated))
                }
                autosaveMutex.withLock {
                    persistDraftSnapshot(currentWorkoutId, _uiState.value)
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Error saving video")
                }
                return@launch
            } catch (e: SecurityException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Cannot access selected video"
                    )
                }
                return@launch
            } catch (e: SQLiteException) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Error saving attempt")
                }
                return@launch
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun setAttemptNotes(attemptId: Int, notes: String) {
        _uiState.update { state ->
            val selectedBoulderId = state.selectedBoulderId ?: return@update state
            val currentAttempts = state.attemptsByBoulderId[selectedBoulderId].orEmpty()
            val updated = state.copy(
                attemptsByBoulderId = state.attemptsByBoulderId + (
                        selectedBoulderId to currentAttempts.map { attempt ->
                            if (attempt.id == attemptId) attempt.copy(notes = notes) else attempt
                        }
                        ),
                errorMessage = null,
            )
            updated.copy(hasChanges = computeHasChanges(updated))
        }
        queueAutosave()
    }

    fun removeAttempt(attemptId: Int) {
        _uiState.update { state ->
            val selectedBoulderId = state.selectedBoulderId ?: return@update state
            val currentAttempts = state.attemptsByBoulderId[selectedBoulderId].orEmpty()
            val updatedAttempts = currentAttempts.filterNot { it.id == attemptId }
            val updated = state.copy(
                attemptsByBoulderId = if (updatedAttempts.isEmpty()) {
                    state.attemptsByBoulderId - selectedBoulderId
                } else {
                    state.attemptsByBoulderId + (selectedBoulderId to updatedAttempts)
                },
                errorMessage = null,
            )
            updated.copy(hasChanges = computeHasChanges(updated))
        }
        queueAutosave()
    }

    fun saveWorkout(onSuccess: () -> Unit) {
        val state = _uiState.value
        val currentWorkoutId = state.workoutId ?: run {
            _uiState.update { it.copy(errorMessage = "Workout is not ready yet") }
            return
        }
        if (state.totalAttemptsCount == 0) {
            _uiState.update { it.copy(errorMessage = "Add at least one attempt") }
            return
        }

        isFinalizing = true
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                autosaveMutex.withLock {
                    val latest = _uiState.value
                    persistDraftSnapshot(
                        workoutId = currentWorkoutId,
                        state = latest,
                        endTimeOverride = LocalTime.now().format(TIME_FORMATTER),
                    )
                }
                _uiState.update { it.copy(isSaving = false, hasChanges = false) }
                originalSnapshot = buildSnapshotFromState(_uiState.value)
                onSuccess()
            } catch (e: SQLiteException) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Error saving workout")
                }
            } finally {
                isFinalizing = false
            }
        }
    }

    private fun loadWorkoutForEdit() {
        viewModelScope.launch {
            val workouts = climbingWorkoutRepository.getAll().first()
            val workout = workouts.firstOrNull { it.id == workoutId }
            if (workout == null) {
                _uiState.update {
                    it.copy(isLoaded = true, errorMessage = "Workout not found")
                }
                return@launch
            }

            val attempts = climbingBoulderAttemptRepository
                .getByClimbingWorkoutId(workoutId)
                .first()
                .sortedWith(compareBy({ it.boulderOrder }, { it.attemptOrder }, { it.id }))
            val mediaById = climbingMediaRepository.getAll().first().associateBy { it.id }
            val attemptsByBoulder = attempts
                .groupBy { it.climbingBoulderId }
                .mapValues { (_, groupedAttempts) ->
                    groupedAttempts.map { attempt ->
                        ClimbingAttemptUiState(
                            id = attempt.id,
                            videoMediaId = attempt.videoMediaId,
                            videoPath = attempt.videoMediaId?.let { mediaById[it]?.filePath },
                            notes = attempt.notes.orEmpty(),
                        )
                    }
                }
            val selectedBoulderIds = attempts
                .map { it.climbingBoulderId }
                .distinct()

            val nextAttemptId = (attempts.maxOfOrNull { it.id } ?: 0) + 1
            val loadedState = _uiState.value.copy(
                workoutId = workout.id,
                date = workout.date,
                startTime = workout.startTime,
                endTime = workout.endTime,
                notes = workout.notes.orEmpty(),
                selectedBoulderId = selectedBoulderIds.firstOrNull(),
                selectedBoulderIds = selectedBoulderIds,
                attemptsByBoulderId = attemptsByBoulder,
                nextAttemptId = nextAttemptId,
                isLoaded = true,
                errorMessage = null,
            )
            originalSnapshot = buildSnapshotFromState(
                loadedState.copy(
                    notes = workout.notes.orEmpty(),
                    date = workout.date,
                    startTime = workout.startTime,
                    endTime = workout.endTime,
                )
            )
            _uiState.value = loadedState.copy(hasChanges = false)
        }
    }

    private fun queueAutosave() {
        if (isFinalizing || isRestoring) return
        viewModelScope.launch {
            val current = _uiState.value
            val currentWorkoutId = current.workoutId ?: return@launch
            if (!current.isLoaded) return@launch
            try {
                autosaveMutex.withLock {
                    if (isFinalizing || isRestoring) return@withLock
                    persistDraftSnapshot(currentWorkoutId, _uiState.value)
                }
            } catch (e: SQLiteException) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Error autosaving workout") }
            }
        }
    }

    private fun computeHasChanges(state: EditClimbingWorkoutUiState): Boolean {
        val snapshot = originalSnapshot ?: return false
        val normalizedNotes = state.notes.trim().ifBlank { null }
        val originalNotes = snapshot.workout.notes?.trim()?.ifBlank { null }
        if (normalizedNotes != originalNotes) return true
        return buildAttemptSnapshots(state) != snapshot.orderedAttempts
    }

    private fun buildAttemptSnapshots(state: EditClimbingWorkoutUiState): List<AttemptSnapshot> {
        val orderedBoulderIds = buildList {
            addAll(state.selectedBoulderIds.filter { boulderId ->
                state.attemptsByBoulderId[boulderId]?.isNotEmpty() == true
            })
            state.attemptsByBoulderId.keys.forEach { boulderId ->
                if (!contains(boulderId)) add(boulderId)
            }
        }
        return orderedBoulderIds.flatMapIndexed { boulderIndex, boulderId ->
            state.attemptsByBoulderId[boulderId].orEmpty().mapIndexed { attemptIndex, attempt ->
                AttemptSnapshot(
                    climbingBoulderId = boulderId,
                    videoMediaId = attempt.videoMediaId,
                    boulderOrder = boulderIndex,
                    attemptOrder = attemptIndex,
                    notes = attempt.notes.trim().ifBlank { null },
                )
            }
        }
    }

    private fun buildSnapshotFromState(state: EditClimbingWorkoutUiState): OriginalSnapshot {
        val currentWorkoutId = state.workoutId ?: workoutId
        return OriginalSnapshot(
            workout = ClimbingWorkout(
                id = currentWorkoutId,
                date = state.date,
                startTime = state.startTime,
                endTime = state.endTime,
                notes = state.notes.trim().ifBlank { null },
            ),
            selectedBoulderIds = state.selectedBoulderIds,
            attemptsByBoulderId = state.attemptsByBoulderId,
            nextAttemptId = state.nextAttemptId,
            orderedAttempts = buildAttemptSnapshots(state),
        )
    }

    private suspend fun restoreOriginalSnapshot(
        workoutId: Int,
        snapshot: OriginalSnapshot,
    ) {
        val existingAttempts =
            climbingBoulderAttemptRepository.getByClimbingWorkoutId(workoutId).first()
        existingAttempts.forEach { attempt ->
            climbingBoulderAttemptRepository.delete(attempt)
        }
        snapshot.orderedAttempts.forEach { attempt ->
            climbingBoulderAttemptRepository.insert(
                ClimbingBoulderAttempt(
                    climbingWorkoutId = workoutId,
                    climbingBoulderId = attempt.climbingBoulderId,
                    videoMediaId = attempt.videoMediaId,
                    boulderOrder = attempt.boulderOrder,
                    attemptOrder = attempt.attemptOrder,
                    notes = attempt.notes,
                )
            )
        }
        climbingWorkoutRepository.update(snapshot.workout)
    }

    private suspend fun persistDraftSnapshot(
        workoutId: Int,
        state: EditClimbingWorkoutUiState,
        endTimeOverride: String? = state.endTime,
    ) {
        val existingAttempts =
            climbingBoulderAttemptRepository.getByClimbingWorkoutId(workoutId).first()
        existingAttempts.forEach { attempt ->
            climbingBoulderAttemptRepository.delete(attempt)
        }

        val orderedBoulderIds = buildList {
            addAll(state.selectedBoulderIds.filter { boulderId ->
                state.attemptsByBoulderId[boulderId]?.isNotEmpty() == true
            })
            state.attemptsByBoulderId.keys.forEach { boulderId ->
                if (!contains(boulderId)) add(boulderId)
            }
        }

        orderedBoulderIds.forEachIndexed { boulderIndex, boulderId ->
            state.attemptsByBoulderId[boulderId].orEmpty().forEachIndexed { attemptIndex, attempt ->
                climbingBoulderAttemptRepository.insert(
                    ClimbingBoulderAttempt(
                        climbingWorkoutId = workoutId,
                        climbingBoulderId = boulderId,
                        videoMediaId = attempt.videoMediaId,
                        boulderOrder = boulderIndex,
                        attemptOrder = attemptIndex,
                        notes = attempt.notes.trim().ifBlank { null },
                    )
                )
            }
        }

        climbingWorkoutRepository.update(
            ClimbingWorkout(
                id = workoutId,
                date = state.date,
                startTime = state.startTime,
                endTime = endTimeOverride,
                notes = state.notes.trim().ifBlank { null },
            )
        )
    }

    @Throws(IOException::class, SecurityException::class, SQLiteException::class)
    private suspend fun saveVideoMedia(
        videoUri: Uri,
        attemptId: Int,
    ): SavedVideoMedia {
        val resolver = appContext.contentResolver
        val mimeType = resolver.getType(videoUri) ?: "video/mp4"
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "mp4"
        val mediaDir = File(appContext.filesDir, "climbing_media")
        if (!mediaDir.exists() && !mediaDir.mkdirs()) {
            throw IOException("Could not create media folder")
        }

        val destination =
            File(mediaDir, "attempt_${System.currentTimeMillis()}_${attemptId}.$extension")
        resolver.openInputStream(videoUri)?.use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IOException("Could not open selected video")

        val mediaId = climbingMediaRepository.insert(
            ClimbingMedia(
                filePath = destination.absolutePath,
                mimeType = mimeType,
            )
        ).toInt()

        return SavedVideoMedia(
            id = mediaId,
            filePath = destination.absolutePath,
        )
    }

    private fun buildBoulderUiState(
        boulders: List<ClimbingBoulder>,
        media: List<ClimbingMedia>,
    ): List<BoulderSelectionUiState> {
        val mediaById = media.associateBy { it.id }
        return boulders.map { boulder ->
            BoulderSelectionUiState(
                id = boulder.id,
                grade = boulder.grade,
                imagePath = boulder.pictureMediaId?.let { mediaById[it]?.filePath },
            )
        }
    }

    private data class SavedVideoMedia(
        val id: Int,
        val filePath: String,
    )

    private data class AttemptSnapshot(
        val climbingBoulderId: Int,
        val videoMediaId: Int?,
        val boulderOrder: Int,
        val attemptOrder: Int,
        val notes: String?,
    )

    private data class OriginalSnapshot(
        val workout: ClimbingWorkout,
        val selectedBoulderIds: List<Int>,
        val attemptsByBoulderId: Map<Int, List<ClimbingAttemptUiState>>,
        val nextAttemptId: Int,
        val orderedAttempts: List<AttemptSnapshot>,
    )

    private companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
