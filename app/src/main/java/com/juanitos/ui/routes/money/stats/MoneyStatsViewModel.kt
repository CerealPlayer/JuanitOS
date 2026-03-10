package com.juanitos.ui.routes.money.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class MoneyStatsSliceType {
    TransactionCategory,
    FixedSpending
}

data class MoneyStatsSlice(
    val type: MoneyStatsSliceType,
    val label: String? = null,
    val amount: Double = 0.0,
)

data class MoneyStatsUiState(
    val hasActiveCycle: Boolean = false,
    val slices: List<MoneyStatsSlice> = emptyList(),
    val totalSpent: Double = 0.0,
) {
    val hasData: Boolean = slices.isNotEmpty()
}

class MoneyStatsViewModel(
    private val cycleRepository: CycleRepository,
    private val fixedSpendingRepository: FixedSpendingRepository,
) : ViewModel() {
    val uiState: StateFlow<MoneyStatsUiState> = combine(
        createCycleFlow(),
        createFixedSpendingsFlow(),
    ) { cycle, fixedSpendings ->
        if (cycle == null) {
            return@combine MoneyStatsUiState()
        }

        val transactionSlices = cycle.transactions
            .groupBy { it.category?.name }
            .map { (categoryName, transactions) ->
                MoneyStatsSlice(
                    type = MoneyStatsSliceType.TransactionCategory,
                    label = categoryName,
                    amount = transactions.sumOf { it.transaction.amount },
                )
            }
            .filter { it.amount > 0.0 }
            .sortedBy { it.label.orEmpty() }

        val fixedSpendingsAmount = fixedSpendings.sumOf { it.fixedSpending.amount }
        val fixedSpendingSlice = if (fixedSpendingsAmount > 0.0) {
            listOf(
                MoneyStatsSlice(
                    type = MoneyStatsSliceType.FixedSpending,
                    amount = fixedSpendingsAmount,
                )
            )
        } else {
            emptyList()
        }

        val slices = transactionSlices + fixedSpendingSlice
        MoneyStatsUiState(
            hasActiveCycle = true,
            slices = slices,
            totalSpent = slices.sumOf { it.amount },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = MoneyStatsUiState(),
    )

    private fun createCycleFlow(): Flow<CurrentCycleWithDetails?> {
        return cycleRepository.getCurrentCycle()
    }

    private fun createFixedSpendingsFlow(): Flow<List<FixedSpendingWithCategory>> {
        return fixedSpendingRepository.getAll().map { spendings ->
            spendings.filter { it.fixedSpending.active }
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

