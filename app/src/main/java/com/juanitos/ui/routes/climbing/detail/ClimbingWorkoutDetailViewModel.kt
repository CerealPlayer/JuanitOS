package com.juanitos.ui.routes.climbing.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class ClimbingWorkoutAttemptDetailUiState(
    val id: Int,
    val notes: String?,
    val videoPath: String?,
)

data class ClimbingWorkoutBoulderSectionUiState(
    val boulderId: Int,
    val grade: String,
    val style: String?,
    val imagePath: String?,
    val attempts: List<ClimbingWorkoutAttemptDetailUiState>,
    val isExpanded: Boolean,
)

data class ClimbingWorkoutDetailUiState(
    val workoutId: Int,
    val notes: String?,
    val sections: List<ClimbingWorkoutBoulderSectionUiState> = emptyList(),
    val workoutFound: Boolean = true,
)

class ClimbingWorkoutDetailViewModel(
    savedStateHandle: SavedStateHandle,
    climbingWorkoutRepository: ClimbingWorkoutRepository,
    climbingBoulderAttemptRepository: ClimbingBoulderAttemptRepository,
    climbingBoulderRepository: ClimbingBoulderRepository,
    climbingMediaRepository: ClimbingMediaRepository,
) : ViewModel() {
    private val workoutId: Int = checkNotNull(savedStateHandle["workoutId"])
    private val expandedBoulderIds = MutableStateFlow<Set<Int>>(emptySet())

    val uiState: StateFlow<ClimbingWorkoutDetailUiState> = combine(
        climbingWorkoutRepository.getAll(),
        climbingBoulderAttemptRepository.getByClimbingWorkoutId(workoutId),
        climbingBoulderRepository.getAll(),
        climbingMediaRepository.getAll(),
        expandedBoulderIds,
    ) { workouts, attempts, boulders, media, expandedIds ->
        val workout = workouts.firstOrNull { it.id == workoutId }
            ?: return@combine ClimbingWorkoutDetailUiState(
                workoutId = workoutId,
                notes = null,
                sections = emptyList(),
                workoutFound = false,
            )

        val mediaById = media.associateBy { it.id }
        val bouldersById = boulders.associateBy { it.id }
        val attemptsSorted = attempts.sortedWith(
            compareBy(
                { it.boulderOrder },
                { it.attemptOrder },
                { it.id },
            )
        )
        val orderedBoulderIds = attemptsSorted
            .map { it.climbingBoulderId }
            .distinct()

        val sections = orderedBoulderIds.mapNotNull { boulderId ->
            val boulder = bouldersById[boulderId] ?: return@mapNotNull null
            val attemptsForBoulder = attemptsSorted
                .filter { it.climbingBoulderId == boulderId }
                .map { attempt ->
                    ClimbingWorkoutAttemptDetailUiState(
                        id = attempt.id,
                        notes = attempt.notes,
                        videoPath = attempt.videoMediaId?.let { mediaById[it]?.filePath },
                    )
                }

            ClimbingWorkoutBoulderSectionUiState(
                boulderId = boulder.id,
                grade = boulder.grade,
                style = boulder.style,
                imagePath = boulder.pictureMediaId?.let { mediaById[it]?.filePath },
                attempts = attemptsForBoulder,
                isExpanded = expandedIds.contains(boulder.id),
            )
        }

        ClimbingWorkoutDetailUiState(
            workoutId = workout.id,
            notes = workout.notes,
            sections = sections,
            workoutFound = true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = ClimbingWorkoutDetailUiState(
            workoutId = workoutId,
            notes = null,
        ),
    )

    fun toggleBoulderSection(boulderId: Int) {
        expandedBoulderIds.update { expanded ->
            if (expanded.contains(boulderId)) expanded - boulderId else expanded + boulderId
        }
    }

    private companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
