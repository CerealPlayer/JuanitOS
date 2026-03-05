package com.juanitos.ui.routes.climbing.boulder

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.climbing.entities.ClimbingBoulder
import com.juanitos.data.climbing.entities.ClimbingMedia
import com.juanitos.data.climbing.repositories.ClimbingBoulderRepository
import com.juanitos.data.climbing.repositories.ClimbingMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

data class NewBoulderUiState(
    val gradeInput: String = "",
    val styleInput: String = "",
    val selectedImageUri: Uri? = null,
    val isGradeValid: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

class NewBoulderViewModel(
    private val climbingBoulderRepository: ClimbingBoulderRepository,
    private val climbingMediaRepository: ClimbingMediaRepository,
    private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewBoulderUiState())
    val uiState: StateFlow<NewBoulderUiState> = _uiState.asStateFlow()

    fun setGradeInput(input: String) {
        _uiState.value = _uiState.value.copy(
            gradeInput = input,
            isGradeValid = input.isNotBlank(),
            errorMessage = null,
        )
    }

    fun setStyleInput(input: String) {
        _uiState.value = _uiState.value.copy(styleInput = input, errorMessage = null)
    }

    fun setSelectedImageUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri, errorMessage = null)
    }

    fun saveBoulder(onSuccess: () -> Unit) {
        val state = _uiState.value
        val grade = state.gradeInput.trim()
        if (grade.isBlank()) {
            _uiState.value = state.copy(isGradeValid = false, errorMessage = "Grade is required")
            return
        }

        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val pictureMediaId = state.selectedImageUri?.let { saveMedia(it) }
                climbingBoulderRepository.insert(
                    ClimbingBoulder(
                        grade = grade,
                        style = state.styleInput.trim().ifBlank { null },
                        pictureMediaId = pictureMediaId,
                    )
                )
                _uiState.value = _uiState.value.copy(isSaving = false)
                onSuccess()
            } catch (e: IOException) {
                _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Error saving image",
                )
            } catch (e: SecurityException) {
                _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Cannot access selected image",
                )
            } catch (e: SQLiteException) {
                _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Error saving boulder",
                )
            }
        }
    }

    @Throws(IOException::class, SecurityException::class, SQLiteException::class)
    private suspend fun saveMedia(uri: Uri): Int {
        val resolver = appContext.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/jpeg"
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        val mediaDir = File(appContext.filesDir, "climbing_media")
        if (!mediaDir.exists() && !mediaDir.mkdirs()) {
            throw IOException("Could not create media folder")
        }

        val destination = File(mediaDir, "boulder_${System.currentTimeMillis()}.$extension")
        resolver.openInputStream(uri)?.use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IOException("Could not open selected image")

        val mediaId = climbingMediaRepository.insert(
            ClimbingMedia(
                filePath = destination.absolutePath,
                mimeType = mimeType,
            )
        )
        return mediaId.toInt()
    }
}
