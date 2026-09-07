package com.juanitos.ui.routes.money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.SavingsGoal
import com.juanitos.data.money.entities.relations.AccountWithDetails
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.CreditCardRepository
import com.juanitos.data.money.repositories.MonthlySummaryRepository
import com.juanitos.data.money.repositories.SavingsGoalRepository
import com.juanitos.data.money.repositories.TransactionRepository
import com.juanitos.lib.MoneyAccountSummary
import com.juanitos.lib.SavingsGoalProjection
import com.juanitos.lib.computeAccountSummary
import com.juanitos.lib.computeSavingsGoalProjection
import com.juanitos.lib.isBalanceAffecting
import com.juanitos.lib.resolveGoalTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class MoneyUiState(
    val summary: MoneyAccountSummary? = null,
    val savingsGoalProjection: SavingsGoalProjection? = null,
)

class MoneyViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository,
    private val monthlySummaryRepository: MonthlySummaryRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MoneyUiState> = createAccountFlow().flatMapLatest { account ->
        if (account == null) {
            flowOf(MoneyUiState())
        } else {
            savingsGoalRepository.getByAccount(account.account.id).map { goal ->
                MoneyUiState(
                    summary = computeAccountSummary(account),
                    savingsGoalProjection = goal?.let { computeGoalProjection(account, it) },
                )
            }
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
                    transactionRepository.applyDuePendingTransactions(account.account.id)
                    monthlySummaryRepository.finalizeElapsedMonths(account.account.id)
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

/**
 * Derives this month's savings pace directly from [account]'s already-loaded transactions (kept
 * live by the same Room Flow [computeAccountSummary] reads), rather than re-querying
 * MonthlySummary, so the goal card updates reactively as transactions change.
 */
private fun computeGoalProjection(
    account: AccountWithDetails,
    goal: SavingsGoal
): SavingsGoalProjection {
    val currentMonthPrefix = YearMonth.now().toString()
    val previousMonthPrefix = YearMonth.now().minusMonths(1).toString()

    val balanceAffecting = account.transactions.filter { isBalanceAffecting(it.transaction) }
    val savedSoFar = balanceAffecting
        .filter { it.transaction.createdAt.orEmpty().startsWith(currentMonthPrefix) }
        .sumOf { -it.transaction.amount }
    val previousMonthIncome = balanceAffecting
        .filter { it.transaction.createdAt.orEmpty().startsWith(previousMonthPrefix) }
        .filter { it.transaction.amount < 0 }
        .sumOf { -it.transaction.amount }

    val target = resolveGoalTarget(goal, previousMonthIncome)
    return computeSavingsGoalProjection(target, savedSoFar)
}
