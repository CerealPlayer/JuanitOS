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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val videoUri: Uri,
    val notes: String = "",
)

data class NewClimbingWorkoutUiState(
    val notes: String = "",
    val showBoulderDialog: Boolean = false,
    val boulders: List<BoulderSelectionUiState> = emptyList(),
    val selectedBoulderId: Int? = null,
    val attempts: List<ClimbingAttemptUiState> = emptyList(),
    val nextAttemptId: Int = 1,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedBoulder: BoulderSelectionUiState?
        get() = boulders.firstOrNull { it.id == selectedBoulderId }

    val canSave: Boolean
        get() = selectedBoulderId != null && attempts.isNotEmpty() && !isSaving
}

class NewClimbingWorkoutViewModel(
    private val climbingWorkoutRepository: ClimbingWorkoutRepository,
    private val climbingBoulderAttemptRepository: ClimbingBoulderAttemptRepository,
    climbingBoulderRepository: ClimbingBoulderRepository,
    private val climbingMediaRepository: ClimbingMediaRepository,
    private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewClimbingWorkoutUiState())
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

    fun setNotes(input: String) {
        _uiState.update { it.copy(notes = input, errorMessage = null) }
    }

    fun openBoulderDialog() {
        _uiState.update { it.copy(showBoulderDialog = true) }
    }

    fun closeBoulderDialog() {
        _uiState.update { it.copy(showBoulderDialog = false) }
    }

    fun selectBoulder(boulderId: Int) {
        _uiState.update {
            it.copy(
                selectedBoulderId = boulderId,
                showBoulderDialog = false,
                errorMessage = null,
            )
        }
    }

    fun addAttemptFromUri(videoUri: Uri) {
        _uiState.update { state ->
            state.copy(
                attempts = state.attempts + ClimbingAttemptUiState(
                    id = state.nextAttemptId,
                    videoUri = videoUri,
                ),
                nextAttemptId = state.nextAttemptId + 1,
                errorMessage = null,
            )
        }
    }

    fun setAttemptNotes(attemptId: Int, notes: String) {
        _uiState.update { state ->
            state.copy(
                attempts = state.attempts.map { attempt ->
                    if (attempt.id == attemptId) {
                        attempt.copy(notes = notes)
                    } else {
                        attempt
                    }
                },
                errorMessage = null,
            )
        }
    }

    fun removeAttempt(attemptId: Int) {
        _uiState.update { state ->
            state.copy(
                attempts = state.attempts.filterNot { it.id == attemptId },
                errorMessage = null,
            )
        }
    }

    fun saveWorkout(onSuccess: () -> Unit) {
        val state = _uiState.value
        val selectedBoulderId = state.selectedBoulderId
        if (selectedBoulderId == null) {
            _uiState.update { it.copy(errorMessage = "Select a boulder before saving") }
            return
        }
        if (state.attempts.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Add at least one attempt") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val now = LocalTime.now().format(TIME_FORMATTER)
                val workoutId = climbingWorkoutRepository.insert(
                    ClimbingWorkout(
                        date = LocalDate.now().toString(),
                        startTime = now,
                        endTime = now,
                        notes = state.notes.trim().ifBlank { null },
                    )
                ).toInt()

                state.attempts.forEach { attempt ->
                    val videoMediaId = saveVideoMedia(attempt)
                    climbingBoulderAttemptRepository.insert(
                        ClimbingBoulderAttempt(
                            climbingWorkoutId = workoutId,
                            climbingBoulderId = selectedBoulderId,
                            videoMediaId = videoMediaId,
                            notes = attempt.notes.trim().ifBlank { null },
                        )
                    )
                }
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Error saving video")
                }
            } catch (e: SecurityException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Cannot access selected video",
                    )
                }
            } catch (e: SQLiteException) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Error saving workout")
                }
            }
        }
    }

    @Throws(IOException::class, SecurityException::class, SQLiteException::class)
    private suspend fun saveVideoMedia(attempt: ClimbingAttemptUiState): Int {
        val resolver = appContext.contentResolver
        val mimeType = resolver.getType(attempt.videoUri) ?: "video/mp4"
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "mp4"
        val mediaDir = File(appContext.filesDir, "climbing_media")
        if (!mediaDir.exists() && !mediaDir.mkdirs()) {
            throw IOException("Could not create media folder")
        }

        val destination = File(
            mediaDir,
            "attempt_${System.currentTimeMillis()}_${attempt.id}.$extension"
        )
        resolver.openInputStream(attempt.videoUri)?.use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IOException("Could not open selected video")

        return climbingMediaRepository.insert(
            ClimbingMedia(
                filePath = destination.absolutePath,
                mimeType = mimeType,
            )
        ).toInt()
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

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
