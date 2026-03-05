package com.juanitos.ui.routes.climbing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.climbing.entities.ClimbingBoulder
import com.juanitos.data.climbing.entities.ClimbingMedia
import com.juanitos.data.climbing.repositories.ClimbingBoulderRepository
import com.juanitos.data.climbing.repositories.ClimbingMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class BoulderSelectionUiState(
    val id: Int,
    val grade: String,
    val imagePath: String?,
)

data class NewClimbingWorkoutUiState(
    val notes: String = "",
    val showBoulderDialog: Boolean = false,
    val boulders: List<BoulderSelectionUiState> = emptyList(),
    val selectedBoulderId: Int? = null,
) {
    val selectedBoulder: BoulderSelectionUiState?
        get() = boulders.firstOrNull { it.id == selectedBoulderId }
}

class NewClimbingWorkoutViewModel(
    climbingBoulderRepository: ClimbingBoulderRepository,
    climbingMediaRepository: ClimbingMediaRepository,
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
        _uiState.update { it.copy(notes = input) }
    }

    fun openBoulderDialog() {
        _uiState.update { it.copy(showBoulderDialog = true) }
    }

    fun closeBoulderDialog() {
        _uiState.update { it.copy(showBoulderDialog = false) }
    }

    fun selectBoulder(boulderId: Int) {
        _uiState.update { it.copy(selectedBoulderId = boulderId, showBoulderDialog = false) }
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
    }
}
