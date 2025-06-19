package com.juanitos.ui.routes.money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import com.juanitos.data.money.repositories.CycleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MoneyViewModel(private val cycleRepository: CycleRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MoneyUiState())
    val uiState: StateFlow<MoneyUiState> = _uiState
        .combine(createCycleFlow()) { state, cycle ->
            state.copy(cycle = cycle)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = MoneyUiState()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createCycleFlow(): Flow<CurrentCycleWithDetails?> {
        return cycleRepository.getCurrentCycle()
    }

    init {
        viewModelScope.launch {
            val currentCycle = cycleRepository.getCurrentCycle().first()
            _uiState.update { it.copy(cycle = currentCycle) }
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class MoneyUiState(
    val cycle: CurrentCycleWithDetails? = null,
)