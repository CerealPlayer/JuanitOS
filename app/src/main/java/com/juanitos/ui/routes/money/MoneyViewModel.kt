package com.juanitos.ui.routes.money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import com.juanitos.data.money.repositories.IncomeScheduleRepository
import com.juanitos.lib.MoneyCycleSummary
import com.juanitos.lib.computeMoneyCycleSummary
import com.juanitos.lib.reconcileCycleWithSchedule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MoneyUiState(
    val summary: MoneyCycleSummary? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MoneyViewModel(
    private val cycleRepository: CycleRepository,
    private val fixedSpendingRepository: FixedSpendingRepository,
    private val incomeScheduleRepository: IncomeScheduleRepository,
) : ViewModel() {
    val uiState: StateFlow<MoneyUiState> = combine(
        createCycleFlow(),
        createFixedSpendingsFlow(),
    ) { cycle, fixedSpendings ->
        if (cycle == null) {
            MoneyUiState()
        } else {
            MoneyUiState(summary = computeMoneyCycleSummary(cycle, fixedSpendings))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = MoneyUiState()
    )

    init {
        viewModelScope.launch {
            reconcileCycleWithSchedule(cycleRepository, incomeScheduleRepository)
        }
    }

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
