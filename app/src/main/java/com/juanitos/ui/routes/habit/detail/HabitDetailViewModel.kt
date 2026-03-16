package com.juanitos.ui.routes.habit.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.habit.entities.Habit
import com.juanitos.data.habit.entities.HabitEntry
import com.juanitos.data.habit.repositories.HabitEntryRepository
import com.juanitos.data.habit.repositories.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    val selectedDate: LocalDate = LocalDate.now(),
    val minSelectableDate: LocalDate = LocalDate.now().minusMonths(1),
    val maxSelectableDate: LocalDate = LocalDate.now(),
)

class HabitDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val habitRepository: HabitRepository,
    private val habitEntryRepository: HabitEntryRepository,
) : ViewModel() {

    private val habitId: Int = checkNotNull(savedStateHandle["habitId"])
    private val maxSelectableDate = LocalDate.now()
    private val minSelectableDate = maxSelectableDate.minusMonths(1)
    private val selectedDate = MutableStateFlow(maxSelectableDate)

    val uiState: StateFlow<HabitDetailUiState> =
        habitRepository.getByIdWithEntries(habitId)
            .combine(selectedDate) { habitWithEntries, selectedDate ->
                val coercedDate = selectedDate.coerceIn(minSelectableDate, maxSelectableDate)
                HabitDetailUiState(
                    habit = habitWithEntries?.habit,
                    weekColumns = buildHabitActivityWeekColumns(
                        entries = habitWithEntries?.entries ?: emptyList(),
                    ),
                    selectedDate = coercedDate,
                    minSelectableDate = minSelectableDate,
                    maxSelectableDate = maxSelectableDate,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HabitDetailUiState(),
            )

    fun onSelectedDateChanged(date: LocalDate) {
        selectedDate.value = date.coerceIn(minSelectableDate, maxSelectableDate)
    }

    fun toggleSelectedDateCompletion() {
        viewModelScope.launch {
            val date = selectedDate.value.coerceIn(minSelectableDate, maxSelectableDate).toString()
            val existing = habitEntryRepository.getByHabitIdAndDate(habitId, date)
            if (existing != null) {
                habitEntryRepository.update(existing.copy(completed = !existing.completed))
                return@launch
            }
            habitEntryRepository.insert(
                HabitEntry(
                    habitId = habitId,
                    date = date,
                    completed = true,
                )
            )
        }
    }

    fun markHabitAsCompleted() {
        viewModelScope.launch {
            val habit = uiState.value.habit ?: return@launch
            if (habit.completedAt != null) return@launch
            habitRepository.update(habit.copy(completedAt = LocalDate.now().toString()))
        }
    }

    fun unmarkHabitAsCompleted() {
        viewModelScope.launch {
            val habit = uiState.value.habit ?: return@launch
            if (habit.completedAt == null) return@launch
            habitRepository.update(habit.copy(completedAt = null))
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.delete(habit)
        }
    }

    companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val WEEKS_TO_KEEP = 26
    }
}

fun buildHabitActivityWeekColumns(
    entries: List<HabitEntry>,
    currentDate: LocalDate = LocalDate.now(),
    weeksToKeep: Int = HabitDetailViewModel.WEEKS_TO_KEEP,
): List<List<HabitActivityCell>> {
    val gridEnd = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    val gridStart = gridEnd
        .minusWeeks(weeksToKeep.toLong() - 1)
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    val completedDates = entries
        .asSequence()
        .filter { it.completed }
        .map { it.date }
        .toSet()

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

    return weekColumns
}
