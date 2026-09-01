package com.juanitos.ui.routes.money.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.relations.AccountWithDetails
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.lib.StatsPeriod
import com.juanitos.lib.isPendingTransaction
import com.juanitos.lib.parseDbDatetimeToLocalDate
import com.juanitos.lib.statsPeriodRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.abs

data class MoneyStatsSlice(
    val label: String? = null,
    val amount: Double = 0.0,
)

data class MoneyStatsUiState(
    val hasSelectedAccount: Boolean = false,
    val selectedPeriod: StatsPeriod = StatsPeriod.MONTH,
    val slices: List<MoneyStatsSlice> = emptyList(),
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
) {
    val hasData: Boolean = slices.isNotEmpty() || totalIncome > 0.0
}

class MoneyStatsViewModel(
    private val accountRepository: AccountRepository,
) : ViewModel() {
    private val selectedPeriod = MutableStateFlow(StatsPeriod.MONTH)

    val uiState: StateFlow<MoneyStatsUiState> = combine(
        createAccountFlow(),
        selectedPeriod,
    ) { account, period ->
        if (account == null) {
            return@combine MoneyStatsUiState(selectedPeriod = period)
        }

        val (start, end) = statsPeriodRange(period)
        val transactionsInPeriod = account.transactions
            .filterNot { isPendingTransaction(it.transaction.createdAt) }
            .filter { transactionWithCategory ->
                val date = parseDbDatetimeToLocalDate(transactionWithCategory.transaction.createdAt)
                    ?: return@filter false
                (start == null || !date.isBefore(start)) && (end == null || !date.isAfter(end))
            }

        val slices = transactionsInPeriod
            .filter { it.transaction.amount > 0.0 }
            .groupBy { it.category?.name }
            .map { (categoryName, transactions) ->
                MoneyStatsSlice(
                    label = categoryName,
                    amount = transactions.sumOf { it.transaction.amount },
                )
            }
            .sortedBy { it.label.orEmpty() }

        val totalIncome = transactionsInPeriod
            .filter { it.transaction.amount < 0.0 }
            .sumOf { abs(it.transaction.amount) }

        MoneyStatsUiState(
            hasSelectedAccount = true,
            selectedPeriod = period,
            slices = slices,
            totalSpent = slices.sumOf { it.amount },
            totalIncome = totalIncome,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = MoneyStatsUiState(),
    )

    private fun createAccountFlow(): Flow<AccountWithDetails?> {
        return accountRepository.getSelected()
    }

    fun setPeriod(period: StatsPeriod) {
        selectedPeriod.value = period
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
