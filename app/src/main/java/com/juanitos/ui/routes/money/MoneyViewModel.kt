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

data class MoneySummary(
    val totalIncome: Double = 0.0,
    val totalFixedSpendings: Double = 0.0,
    val totalTransactions: Double = 0.0,
    val remaining: Double = 0.0,
)

data class MoneyUiState(
    val cycle: CurrentCycleWithDetails? = null,
    val summary: MoneySummary = MoneySummary(),
)

class MoneyViewModel(private val cycleRepository: CycleRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MoneyUiState())
    val uiState: StateFlow<MoneyUiState> = _uiState
        .combine(createCycleFlow()) { state, cycle ->
            val summary = if (cycle != null) {
                val totalIncome = cycle.cycle.totalIncome
                val totalTransactions = cycle.transactions.sumOf { it.transaction.amount }
                val remaining = totalIncome - totalTransactions
                MoneySummary(
                    totalIncome = totalIncome,
                    totalTransactions = totalTransactions,
                    remaining = remaining
                )
            } else {
                MoneySummary()
            }
            state.copy(cycle = cycle, summary = summary)
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
