package com.juanitos.ui.routes.money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MoneySummary(
    val totalIncome: Double = 0.0,
    val totalFixedSpendings: Double = 0.0,
    val totalTransactions: Double = 0.0,
    val remaining: Double = 0.0,
)

data class MoneyUiState(
    val cycle: CurrentCycleWithDetails? = null,
    val summary: MoneySummary = MoneySummary(),
    val fixedSpendings: List<FixedSpendingWithCategory> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class MoneyViewModel(
    private val cycleRepository: CycleRepository,
    private val fixedSpendingRepository: FixedSpendingRepository
) : ViewModel() {
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
        }.combine(createFixedSpendingsFlow()) { state, fixedSpendings ->
            val totalFixedSpendings = fixedSpendings.sumOf { it.fixedSpending.amount }
            val remainingAfterFixed = state.summary.remaining - totalFixedSpendings
            val updatedSummary = state.summary.copy(
                totalFixedSpendings = totalFixedSpendings,
                remaining = remainingAfterFixed
            )
            state.copy(summary = updatedSummary, fixedSpendings = fixedSpendings)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = MoneyUiState()
        )

    private fun createCycleFlow(): Flow<CurrentCycleWithDetails?> {
        return cycleRepository.getCurrentCycle()
    }

    private fun createFixedSpendingsFlow(): Flow<List<FixedSpendingWithCategory>> {
        return fixedSpendingRepository.getAll().map {
            it.filter { s -> s.fixedSpending.active }
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
