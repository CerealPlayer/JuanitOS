package com.juanitos.ui.routes.money.creditcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Account
import com.juanitos.data.money.entities.CreditCard
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.CreditCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CreditCardsUiState(
    val creditCards: List<CreditCard> = emptyList(),
    val accounts: List<Account> = emptyList(),
)

class CreditCardsViewModel(
    private val creditCardRepository: CreditCardRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreditCardsUiState())
    val uiState = _uiState
        .combine(creditCardRepository.getAll()) { state, creditCards ->
            state.copy(creditCards = creditCards)
        }
        .combine(accountRepository.getAll()) { state, accounts ->
            state.copy(accounts = accounts)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = CreditCardsUiState()
        )

    fun deleteCreditCard(creditCard: CreditCard) {
        viewModelScope.launch {
            creditCardRepository.delete(creditCard)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
