package com.juanitos.ui.routes.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.habit.entities.HabitEntry
import com.juanitos.data.habit.entities.relations.HabitWithEntries
import com.juanitos.data.habit.repositories.HabitEntryRepository
import com.juanitos.data.habit.repositories.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitsUiState(
    val habitsWithEntries: List<HabitWithEntries> = emptyList(),
    val today: String = LocalDate.now().toString(),
)

class HabitsViewModel(
    private val habitRepository: HabitRepository,
    private val habitEntryRepository: HabitEntryRepository,
) : ViewModel() {

    val uiState: StateFlow<HabitsUiState> = habitRepository.getAllWithEntries()
        .map { HabitsUiState(habitsWithEntries = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HabitsUiState()
        )

    fun toggleEntry(habitId: Int, date: String, currentCompleted: Boolean) {
        viewModelScope.launch {
            val existing = habitEntryRepository.getByHabitIdAndDate(habitId, date)
            if (existing != null) {
                habitEntryRepository.update(existing.copy(completed = !currentCompleted))
            } else {
                habitEntryRepository.insert(
                    HabitEntry(habitId = habitId, date = date, completed = true)
                )
            }
        }
    }
}
