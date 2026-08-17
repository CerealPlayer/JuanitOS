package com.juanitos.ui.routes.money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import com.juanitos.data.money.repositories.IncomeScheduleRepository
import com.juanitos.data.money.repositories.TransactionRepository
import com.juanitos.lib.clampDayOfMonth
import com.juanitos.lib.parseDbDatetimeToLocalDate
import com.juanitos.lib.reconcileCycleWithSchedule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class MoneyUiState(
    val cycle: CurrentCycleWithDetails? = null,
    val fixedSpendings: List<FixedSpendingWithCategory> = emptyList(),
    val movements: List<Movement> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class MoneyViewModel(
    private val cycleRepository: CycleRepository,
    private val fixedSpendingRepository: FixedSpendingRepository,
    private val transactionRepository: TransactionRepository,
    private val incomeScheduleRepository: IncomeScheduleRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MoneyUiState())
    val uiState: StateFlow<MoneyUiState> = _uiState
        .combine(createCycleFlow()) { state, cycle ->
            state.copy(cycle = cycle)
        }.combine(createFixedSpendingsFlow()) { state, fixedSpendings ->
            state.copy(
                fixedSpendings = fixedSpendings,
                movements = mergeMovements(state.cycle, fixedSpendings)
            )
        }
        .stateIn(
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

    private fun mergeMovements(
        cycle: CurrentCycleWithDetails?,
        fixedSpendings: List<FixedSpendingWithCategory>
    ): List<Movement> {
        val cycleMonth = cycle?.cycle?.startDate
            ?.let(::parseDbDatetimeToLocalDate)
            ?.let(YearMonth::from)
            ?: YearMonth.now()

        val fixedSpendingMovements = fixedSpendings.map { fixedSpending ->
            Movement.FixedSpendingMovement(
                fixedSpending = fixedSpending,
                date = fixedSpending.fixedSpending.dayOfMonth?.let {
                    clampDayOfMonth(
                        cycleMonth,
                        it
                    )
                }
            )
        }
        val transactionMovements = (cycle?.transactions ?: emptyList())
            .map { Movement.TransactionMovement(it) }

        // Undated fixed spendings sort first (matches pre-scheduling behavior), then everything
        // else interleaved by date ascending, ties broken fixed-spending-before-transaction.
        return (fixedSpendingMovements + transactionMovements).sortedWith(
            compareBy(
                { it.date != null },
                { it.date },
                { movementTypePriority(it) },
                { it.sortId }
            )
        )
    }

    private fun movementTypePriority(movement: Movement): Int = when (movement) {
        is Movement.FixedSpendingMovement -> 0
        is Movement.TransactionMovement -> 1
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.delete(transaction)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
