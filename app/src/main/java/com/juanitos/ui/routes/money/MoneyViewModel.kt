package com.juanitos.ui.routes.money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.relations.AccountWithDetails
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.CreditCardRepository
import com.juanitos.data.money.repositories.TransactionRepository
import com.juanitos.lib.MoneyAccountSummary
import com.juanitos.lib.computeAccountSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MoneyUiState(
    val summary: MoneyAccountSummary? = null,
)

class MoneyViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository,
) : ViewModel() {
    val uiState: StateFlow<MoneyUiState> = createAccountFlow().map { account ->
        if (account == null) {
            MoneyUiState()
        } else {
            MoneyUiState(summary = computeAccountSummary(account))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = MoneyUiState()
    )

    init {
        viewModelScope.launch {
            accountRepository.getSelected().collect { account ->
                if (account != null) {
                    transactionRepository.generateDueOccurrences(account.account.id)
                    creditCardRepository.generateDueSettlements(account.account.id)
                }
            }
        }
    }

    private fun createAccountFlow(): Flow<AccountWithDetails?> {
        return accountRepository.getSelected()
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
