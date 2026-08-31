package com.juanitos.ui.routes.money.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.entities.relations.AccountWithDetails
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.TransactionRepository
import com.juanitos.lib.parseQtDouble
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Estado de la UI para el formulario de transacción
data class NewTransactionUiState(
    val amountInput: String = "",
    val isAmountValid: Boolean = true,
    val categoryId: Int = 0,
    val descriptionInput: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val selectedAccountId: Int? = null,
    val categories: List<Category> = emptyList()
)

class NewTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewTransactionUiState())
    val uiState: StateFlow<NewTransactionUiState> =
        _uiState.combine(createSelectedAccountFlow()) { state, account ->
            state.copy(selectedAccountId = account?.account?.id)
        }.combine(createCategoriesFlow()) { state, categories ->
            state.copy(categories = categories)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NewTransactionUiState()
        )

    private fun createSelectedAccountFlow(): Flow<AccountWithDetails?> {
        return accountRepository.getSelected()
    }

    private fun createCategoriesFlow(): Flow<List<Category>> {
        return categoryRepository.getAll()
    }

    fun setAmountInput(input: String) {
        _uiState.value = _uiState.value.copy(
            amountInput = input,
            isAmountValid = parseQtDouble(input) != null,
            errorMessage = null
        )
    }

    fun setCategoryId(input: Int) {
        _uiState.value = _uiState.value.copy(categoryId = input, errorMessage = null)
    }

    fun setDescriptionInput(input: String) {
        _uiState.value = _uiState.value.copy(descriptionInput = input)
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val state = uiState.value
        val amount = parseQtDouble(state.amountInput)
        val category = state.categoryId
        val accountId = state.selectedAccountId
        if (amount == null) {
            _uiState.value = state.copy(isAmountValid = false, errorMessage = "Invalid amount")
            return
        }
        if (category <= 0) {
            _uiState.value = state.copy(errorMessage = "Category required")
            return
        }
        if (accountId == null) {
            _uiState.value = state.copy(errorMessage = "No account selected")
            return
        }
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                transactionRepository.insert(
                    Transaction(
                        accountId = accountId,
                        amount = amount,
                        categoryId = category,
                        description = state.descriptionInput
                    )
                )
                _uiState.value = state.copy(success = true, isSaving = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Error saving transaction"
                )
            }
        }
    }
}
