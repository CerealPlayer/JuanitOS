package com.juanitos.ui.routes.money.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.CreditCard
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.entities.relations.AccountWithDetails
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.CreditCardRepository
import com.juanitos.data.money.repositories.TransactionRepository
import com.juanitos.lib.TransactionFrequency
import com.juanitos.lib.formatDbDatetimeToShortDate
import com.juanitos.lib.formatLocalDateToDbDatetime
import com.juanitos.lib.parseQtDouble
import com.juanitos.lib.parseShortDateToLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

// Estado de la UI para el formulario de transacción
data class NewTransactionUiState(
    val amountInput: String = "",
    val isAmountValid: Boolean = true,
    val categoryId: Int = 0,
    val descriptionInput: String = "",
    val dateInput: String = formatDbDatetimeToShortDate(formatLocalDateToDbDatetime(LocalDate.now())),
    val isIncome: Boolean = false,
    val frequency: TransactionFrequency? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val selectedAccountId: Int? = null,
    val categories: List<Category> = emptyList(),
    val isPaidWithCredit: Boolean = false,
    val creditCardId: Int? = null,
    val creditCards: List<CreditCard> = emptyList()
)

class NewTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val creditCardRepository: CreditCardRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewTransactionUiState())
    val uiState: StateFlow<NewTransactionUiState> =
        _uiState.combine(createSelectedAccountFlow()) { state, account ->
            state.copy(selectedAccountId = account?.account?.id)
        }.combine(createCategoriesFlow()) { state, categories ->
            state.copy(categories = categories)
        }.combine(createCreditCardsFlow()) { state, creditCards ->
            state.copy(creditCards = creditCards)
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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun createCreditCardsFlow(): Flow<List<CreditCard>> =
        createSelectedAccountFlow().flatMapLatest { account ->
            if (account == null) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                creditCardRepository.getByAccountId(account.account.id)
            }
        }

    fun setAmountInput(input: String) {
        val amount = parseQtDouble(input)
        _uiState.value = _uiState.value.copy(
            amountInput = input,
            isAmountValid = amount != null && amount >= 0,
            errorMessage = null
        )
    }

    fun setIsIncome(isIncome: Boolean) {
        _uiState.value = _uiState.value.copy(isIncome = isIncome)
    }

    fun setCategoryId(input: Int) {
        _uiState.value = _uiState.value.copy(categoryId = input, errorMessage = null)
    }

    fun setDescriptionInput(input: String) {
        _uiState.value = _uiState.value.copy(descriptionInput = input)
    }

    fun setDateInput(input: String) {
        _uiState.value = _uiState.value.copy(dateInput = input, errorMessage = null)
    }

    fun setFrequency(frequency: TransactionFrequency?) {
        _uiState.value = _uiState.value.copy(frequency = frequency)
    }

    fun setIsPaidWithCredit(isPaidWithCredit: Boolean) {
        _uiState.value = _uiState.value.copy(
            isPaidWithCredit = isPaidWithCredit,
            creditCardId = if (isPaidWithCredit) _uiState.value.creditCardId else null,
            errorMessage = null
        )
    }

    fun setCreditCardId(input: Int) {
        _uiState.value = _uiState.value.copy(creditCardId = input, errorMessage = null)
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val state = uiState.value
        val amount = parseQtDouble(state.amountInput)
        val category = state.categoryId
        val accountId = state.selectedAccountId
        val date = parseShortDateToLocalDate(state.dateInput)
        if (amount == null || amount < 0) {
            _uiState.value = state.copy(isAmountValid = false, errorMessage = "Invalid amount")
            return
        }
        val signedAmount = if (state.isIncome) -amount else amount
        if (category <= 0) {
            _uiState.value = state.copy(errorMessage = "Category required")
            return
        }
        if (accountId == null) {
            _uiState.value = state.copy(errorMessage = "No account selected")
            return
        }
        if (date == null) {
            _uiState.value = state.copy(errorMessage = "Invalid date")
            return
        }
        if (state.isPaidWithCredit && state.creditCardId == null) {
            _uiState.value = state.copy(errorMessage = "Credit card required")
            return
        }
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                transactionRepository.insert(
                    Transaction(
                        accountId = accountId,
                        amount = signedAmount,
                        categoryId = category,
                        description = state.descriptionInput,
                        createdAt = formatLocalDateToDbDatetime(date),
                        frequency = state.frequency?.name,
                        creditCardId = if (state.isPaidWithCredit) state.creditCardId else null
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
