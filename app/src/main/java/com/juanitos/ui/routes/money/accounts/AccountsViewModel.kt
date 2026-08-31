package com.juanitos.ui.routes.money.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Account
import com.juanitos.data.money.repositories.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
)

class AccountsViewModel(private val accountRepository: AccountRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState = _uiState.combine(accountRepository.getAll()) { state, accounts ->
        state.copy(accounts = accounts)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = AccountsUiState()
    )

    fun selectAccount(id: Int) {
        viewModelScope.launch {
            accountRepository.selectAccount(id)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountRepository.delete(account)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
