package com.juanitos.ui.routes.habit.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.habit.entities.Habit
import com.juanitos.data.habit.entities.HabitEntry
import com.juanitos.data.habit.repositories.HabitEntryRepository
import com.juanitos.data.habit.repositories.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class HabitActivityCell(
    val date: LocalDate,
    val isFutureDate: Boolean,
    val isCompleted: Boolean,
)

data class HabitDetailUiState(
    val habit: Habit? = null,
    val weekColumns: List<List<HabitActivityCell>> = emptyList(),
)

class HabitDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val habitRepository: HabitRepository,
    private val habitEntryRepository: HabitEntryRepository,
) : ViewModel() {

    private val habitId: Int = checkNotNull(savedStateHandle["habitId"])

    val uiState: StateFlow<HabitDetailUiState> =
        habitRepository.getByIdWithEntries(habitId)
            .map { habitWithEntries ->
                val currentDate = LocalDate.now()
                val gridEnd = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val gridStart = gridEnd
                    .minusWeeks(WEEKS_TO_KEEP.toLong() - 1)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

                val completedDates = habitWithEntries?.entries
                    ?.asSequence()
                    ?.filter { it.completed }
                    ?.map { it.date }
                    ?.toSet()
                    ?: emptySet()

                val weekColumns = mutableListOf<List<HabitActivityCell>>()
                var weekStart = gridStart
                while (!weekStart.isAfter(gridEnd)) {
                    weekColumns += (0..6).map { dayOffset ->
                        val day = weekStart.plusDays(dayOffset.toLong())
                        HabitActivityCell(
                            date = day,
                            isFutureDate = day.isAfter(currentDate),
                            isCompleted = !day.isAfter(currentDate) && completedDates.contains(day.toString()),
                        )
                    }
                    weekStart = weekStart.plusWeeks(1)
                }

                HabitDetailUiState(
                    habit = habitWithEntries?.habit,
                    weekColumns = weekColumns,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HabitDetailUiState(),
            )

    fun markCompletedToday() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val existing = habitEntryRepository.getByHabitIdAndDate(habitId, today)
            if (existing != null) {
                if (!existing.completed) {
                    habitEntryRepository.update(existing.copy(completed = true))
                }
                return@launch
            }
            habitEntryRepository.insert(
                HabitEntry(
                    habitId = habitId,
                    date = today,
                    completed = true,
                )
            )
        }
    }

    companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val WEEKS_TO_KEEP = 26
    }
}
