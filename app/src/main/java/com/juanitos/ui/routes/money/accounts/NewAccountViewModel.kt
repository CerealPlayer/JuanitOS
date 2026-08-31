package com.juanitos.ui.routes.money.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.lib.parseQtDouble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class NewAccountUiState(
    val nameInput: String = "",
    val isNameValid: Boolean = true,
    val startingBalanceInput: String = "",
    val isStartingBalanceValid: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
)

class NewAccountViewModel(
    private val accountRepository: AccountRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewAccountUiState())
    val uiState: StateFlow<NewAccountUiState> = _uiState.asStateFlow()

    fun setNameInput(input: String) {
        _uiState.value = _uiState.value.copy(
            nameInput = input,
            isNameValid = input.isNotBlank(),
            errorMessage = null
        )
    }

    fun setStartingBalanceInput(input: String) {
        _uiState.value = _uiState.value.copy(
            startingBalanceInput = input,
            isStartingBalanceValid = input.isBlank() || parseQtDouble(input) != null,
            errorMessage = null
        )
    }

    fun saveAccount(onSuccess: () -> Unit) {
        val state = _uiState.value
        val name = state.nameInput
        if (name.isBlank()) {
            _uiState.value = state.copy(isNameValid = false, errorMessage = "Name cannot be empty")
            return
        }
        val startingBalance = if (state.startingBalanceInput.isBlank()) {
            0.0
        } else {
            parseQtDouble(state.startingBalanceInput)
        }
        if (startingBalance == null) {
            _uiState.value = state.copy(
                isStartingBalanceValid = false,
                errorMessage = "Invalid starting balance"
            )
            return
        }
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val isFirstAccount = accountRepository.getAll().first().isEmpty()
                val id = accountRepository.insert(name, startingBalance)
                if (isFirstAccount) {
                    accountRepository.selectAccount(id.toInt())
                }
                _uiState.value = _uiState.value.copy(isSaving = false, success = true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Error saving account"
                )
            }
        }
    }
}
