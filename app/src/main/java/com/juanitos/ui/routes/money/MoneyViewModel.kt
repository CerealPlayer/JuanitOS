package com.juanitos.ui.routes.money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.relations.AccountWithDetails
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import com.juanitos.lib.MoneyAccountSummary
import com.juanitos.lib.computeAccountSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MoneyUiState(
    val summary: MoneyAccountSummary? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MoneyViewModel(
    private val accountRepository: AccountRepository,
    private val fixedSpendingRepository: FixedSpendingRepository,
) : ViewModel() {
    val uiState: StateFlow<MoneyUiState> = combine(
        createAccountFlow(),
        createFixedSpendingsFlow(),
    ) { account, fixedSpendings ->
        if (account == null) {
            MoneyUiState()
        } else {
            MoneyUiState(summary = computeAccountSummary(account, fixedSpendings))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = MoneyUiState()
    )

    private fun createAccountFlow(): Flow<AccountWithDetails?> {
        return accountRepository.getSelected()
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
