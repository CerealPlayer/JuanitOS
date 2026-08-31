package com.juanitos.ui.routes.money.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.relations.AccountWithDetails
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.lib.isPendingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MoneyStatsSlice(
    val label: String? = null,
    val amount: Double = 0.0,
)

data class MoneyStatsUiState(
    val hasSelectedAccount: Boolean = false,
    val slices: List<MoneyStatsSlice> = emptyList(),
    val totalSpent: Double = 0.0,
) {
    val hasData: Boolean = slices.isNotEmpty()
}

class MoneyStatsViewModel(
    private val accountRepository: AccountRepository,
) : ViewModel() {
    val uiState: StateFlow<MoneyStatsUiState> = createAccountFlow().map { account ->
        if (account == null) {
            return@map MoneyStatsUiState()
        }

        val slices = account.transactions
            .filterNot { isPendingTransaction(it.transaction.createdAt) }
            .groupBy { it.category?.name }
            .map { (categoryName, transactions) ->
                MoneyStatsSlice(
                    label = categoryName,
                    amount = transactions.sumOf { it.transaction.amount },
                )
            }
            .filter { it.amount > 0.0 }
            .sortedBy { it.label.orEmpty() }

        MoneyStatsUiState(
            hasSelectedAccount = true,
            slices = slices,
            totalSpent = slices.sumOf { it.amount },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = MoneyStatsUiState(),
    )

    private fun createAccountFlow(): Flow<AccountWithDetails?> {
        return accountRepository.getSelected()
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
