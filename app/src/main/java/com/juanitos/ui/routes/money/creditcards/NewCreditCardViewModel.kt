package com.juanitos.ui.routes.money.creditcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Account
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.CreditCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NewCreditCardUiState(
    val nameInput: String = "",
    val isNameValid: Boolean = true,
    val paymentDayInput: String = "",
    val isPaymentDayValid: Boolean = true,
    val accountId: Int? = null,
    val accounts: List<Account> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
)

class NewCreditCardViewModel(
    private val creditCardRepository: CreditCardRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewCreditCardUiState())
    val uiState: StateFlow<NewCreditCardUiState> =
        _uiState.combine(accountRepository.getAll()) { state, accounts ->
            state.copy(
                accounts = accounts,
                accountId = state.accountId ?: accounts.firstOrNull()?.id
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NewCreditCardUiState()
        )

    fun setNameInput(input: String) {
        _uiState.value = _uiState.value.copy(
            nameInput = input,
            isNameValid = input.isNotBlank(),
            errorMessage = null
        )
    }

    fun setPaymentDayInput(input: String) {
        val day = input.toIntOrNull()
        _uiState.value = _uiState.value.copy(
            paymentDayInput = input,
            isPaymentDayValid = day != null && day in 1..31,
            errorMessage = null
        )
    }

    fun setAccountId(accountId: Int) {
        _uiState.value = _uiState.value.copy(accountId = accountId, errorMessage = null)
    }

    fun saveCreditCard(onSuccess: () -> Unit) {
        val state = uiState.value
        val name = state.nameInput
        val paymentDay = state.paymentDayInput.toIntOrNull()
        val accountId = state.accountId
        if (name.isBlank()) {
            _uiState.value = state.copy(isNameValid = false, errorMessage = "Name cannot be empty")
            return
        }
        if (paymentDay == null || paymentDay !in 1..31) {
            _uiState.value = state.copy(
                isPaymentDayValid = false,
                errorMessage = "Payment day must be between 1 and 31"
            )
            return
        }
        if (accountId == null) {
            _uiState.value = state.copy(errorMessage = "No account selected")
            return
        }
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                creditCardRepository.insert(accountId, name, paymentDay)
                _uiState.value = _uiState.value.copy(isSaving = false, success = true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Error saving credit card"
                )
            }
        }
    }
}
