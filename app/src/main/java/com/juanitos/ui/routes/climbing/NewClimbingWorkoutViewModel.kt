package com.juanitos.ui.routes.climbing

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.webkit.MimeTypeMap
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class BoulderSelectionUiState(
    val id: Int,
    val grade: String,
    val imagePath: String?,
)

data class ClimbingAttemptUiState(
    val id: Int,
    val videoMediaId: Int?,
    val videoPath: String?,
    val notes: String = "",
)

data class NewClimbingWorkoutUiState(
    val workoutId: Int? = null,
    val date: String = "",
    val startTime: String? = null,
    val notes: String = "",
    val showBoulderDialog: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val boulders: List<BoulderSelectionUiState> = emptyList(),
    val selectedBoulderId: Int? = null,
    val selectedBoulderIds: List<Int> = emptyList(),
    val attemptsByBoulderId: Map<Int, List<ClimbingAttemptUiState>> = emptyMap(),
    val nextAttemptId: Int = 1,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedBoulder: BoulderSelectionUiState?
        get() = boulders.firstOrNull { it.id == selectedBoulderId }

    val selectedBoulderAttempts: List<ClimbingAttemptUiState>
        get() = selectedBoulderId?.let { attemptsByBoulderId[it] }.orEmpty()

    val totalAttemptsCount: Int
        get() = attemptsByBoulderId.values.sumOf { it.size }

    val hasContent: Boolean
        get() = notes.isNotBlank() || totalAttemptsCount > 0

    val canSave: Boolean
        get() = workoutId != null && totalAttemptsCount > 0 && !isSaving
}

class NewClimbingWorkoutViewModel(
    private val climbingWorkoutRepository: ClimbingWorkoutRepository,
    private val climbingBoulderAttemptRepository: ClimbingBoulderAttemptRepository,
    climbingBoulderRepository: ClimbingBoulderRepository,
    private val climbingMediaRepository: ClimbingMediaRepository,
    private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewClimbingWorkoutUiState())
    private val autosaveMutex = Mutex()
    private var isFinalizing = false

    val uiState: StateFlow<NewClimbingWorkoutUiState> =
        combine(
            _uiState,
            climbingBoulderRepository.getAll(),
            climbingMediaRepository.getAll(),
        ) { state, boulders, media ->
            state.copy(boulders = buildBoulderUiState(boulders, media))
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = NewClimbingWorkoutUiState(),
            )

    init {
        createDraftWorkout()
    }

    fun setNotes(input: String) {
        _uiState.update { it.copy(notes = input, errorMessage = null) }
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

    fun discardWorkout(onSuccess: () -> Unit) {
        val current = _uiState.value
        val workoutId = current.workoutId ?: run { onSuccess(); return }
        viewModelScope.launch {
            climbingWorkoutRepository.delete(
                ClimbingWorkout(
                    id = workoutId,
                    date = current.date,
                    startTime = current.startTime,
                    notes = current.notes.trim().ifBlank { null },
                )
            )
            onSuccess()
        }
    }

    fun selectBoulder(boulderId: Int) {
        _uiState.update {
            it.copy(
                selectedBoulderId = boulderId,
                selectedBoulderIds = if (it.selectedBoulderIds.contains(boulderId)) {
                    it.selectedBoulderIds
                } else {
                    it.selectedBoulderIds + boulderId
                },
                showBoulderDialog = false,
                errorMessage = null,
            )
        }
        queueAutosave()
    }

    fun addAttemptFromUri(videoUri: Uri) {
        val current = _uiState.value
        val selectedBoulderId = current.selectedBoulderId ?: run {
            _uiState.update { it.copy(errorMessage = "Select a boulder before adding an attempt") }
            return
        }
        val workoutId = current.workoutId ?: run {
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
                    state.copy(
                        attemptsByBoulderId = state.attemptsByBoulderId + (
                                selectedBoulderId to (latestAttempts + ClimbingAttemptUiState(
                                    id = attemptId,
                                    videoMediaId = savedMedia.id,
                                    videoPath = savedMedia.filePath,
                                ))
                                ),
                        nextAttemptId = attemptId + 1,
                        errorMessage = null,
                    )
                }
                autosaveMutex.withLock {
                    persistDraftSnapshot(workoutId, _uiState.value)
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
                        errorMessage = e.message ?: "Cannot access selected video",
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
            state.copy(
                attemptsByBoulderId = state.attemptsByBoulderId + (
                        selectedBoulderId to currentAttempts.map { attempt ->
                            if (attempt.id == attemptId) attempt.copy(notes = notes) else attempt
                    }
                        ),
                errorMessage = null,
            )
        }
        queueAutosave()
    }

    fun removeAttempt(attemptId: Int) {
        _uiState.update { state ->
            val selectedBoulderId = state.selectedBoulderId ?: return@update state
            val currentAttempts = state.attemptsByBoulderId[selectedBoulderId].orEmpty()
            val updatedAttempts = currentAttempts.filterNot { it.id == attemptId }
            state.copy(
                attemptsByBoulderId = if (updatedAttempts.isEmpty()) {
                    state.attemptsByBoulderId - selectedBoulderId
                } else {
                    state.attemptsByBoulderId + (selectedBoulderId to updatedAttempts)
                },
                errorMessage = null,
            )
        }
        queueAutosave()
    }

    fun saveWorkout(onSuccess: () -> Unit) {
        val state = _uiState.value
        val workoutId = state.workoutId ?: run {
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
                    persistDraftSnapshot(workoutId, latest)
                    climbingWorkoutRepository.update(
                        ClimbingWorkout(
                            id = workoutId,
                            date = latest.date,
                            startTime = latest.startTime,
                            endTime = LocalTime.now().format(TIME_FORMATTER),
                            notes = latest.notes.trim().ifBlank { null },
                        )
                    )
                }
                onSuccess()
            } catch (e: SQLiteException) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Error saving workout")
                }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun createDraftWorkout() {
        viewModelScope.launch {
            val date = LocalDate.now().toString()
            val startTime = LocalTime.now().format(TIME_FORMATTER)
            try {
                val workoutId = climbingWorkoutRepository.insert(
                    ClimbingWorkout(
                        date = date,
                        startTime = startTime,
                        notes = null,
                    )
                ).toInt()
                _uiState.update {
                    it.copy(
                        workoutId = workoutId,
                        date = date,
                        startTime = startTime,
                    )
                }
            } catch (e: SQLiteException) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Error creating workout draft"
                    )
                }
            }
        }
    }

    private fun queueAutosave() {
        if (isFinalizing) return
        viewModelScope.launch {
            val workoutId = _uiState.value.workoutId ?: return@launch
            try {
                autosaveMutex.withLock {
                    if (isFinalizing) return@withLock
                    persistDraftSnapshot(workoutId, _uiState.value)
                }
            } catch (e: SQLiteException) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Error autosaving workout") }
            }
        }
    }

    private suspend fun persistDraftSnapshot(
        workoutId: Int,
        state: NewClimbingWorkoutUiState,
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
            state.attemptsByBoulderId[boulderId].orEmpty()
                .forEachIndexed { attemptIndex, attempt ->
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
                endTime = null,
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

        val destination = File(
            mediaDir,
            "attempt_${System.currentTimeMillis()}_${attemptId}.$extension"
        )
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

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
