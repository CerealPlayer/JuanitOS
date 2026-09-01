package com.juanitos.ui.routes.money.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.TransactionRepository
import com.juanitos.lib.TransactionFrequency
import com.juanitos.lib.formatDbDatetimeToShortDate
import com.juanitos.lib.formatLocalDateToDbDatetime
import com.juanitos.lib.parseQtDouble
import com.juanitos.lib.parseShortDateToLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class NewAccountStep {
    DETAILS,
    RECURRING_INCOME,
    RECURRING_EXPENSE,
    SUMMARY,
}

data class DraftTransaction(
    val localId: Int,
    val isIncome: Boolean,
    val amount: Double,
    val categoryId: Int,
    val categoryName: String,
    val description: String,
    val date: LocalDate,
    val frequency: TransactionFrequency,
)

data class NewAccountUiState(
    val step: NewAccountStep = NewAccountStep.DETAILS,
    val nameInput: String = "",
    val isNameValid: Boolean = true,
    val startingBalanceInput: String = "",
    val isStartingBalanceValid: Boolean = true,
    val draftAmountInput: String = "",
    val isDraftAmountValid: Boolean = true,
    val draftCategoryId: Int = 0,
    val draftDescriptionInput: String = "",
    val draftDateInput: String = formatDbDatetimeToShortDate(
        formatLocalDateToDbDatetime(LocalDate.now())
    ),
    val draftFrequency: TransactionFrequency = TransactionFrequency.MONTHLY,
    val incomes: List<DraftTransaction> = emptyList(),
    val expenses: List<DraftTransaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val nextDraftId: Int = 0,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
)

class NewAccountViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewAccountUiState())
    val uiState: StateFlow<NewAccountUiState> =
        _uiState.combine(categoryRepository.getAll()) { state, categories ->
            state.copy(categories = categories)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NewAccountUiState()
        )

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

    fun setDraftAmountInput(input: String) {
        val amount = parseQtDouble(input)
        _uiState.value = _uiState.value.copy(
            draftAmountInput = input,
            isDraftAmountValid = amount != null && amount >= 0,
            errorMessage = null
        )
    }

    fun setDraftCategoryId(input: Int) {
        _uiState.value = _uiState.value.copy(draftCategoryId = input, errorMessage = null)
    }

    fun setDraftDescriptionInput(input: String) {
        _uiState.value = _uiState.value.copy(draftDescriptionInput = input)
    }

    fun setDraftDateInput(input: String) {
        _uiState.value = _uiState.value.copy(draftDateInput = input, errorMessage = null)
    }

    fun setDraftFrequency(frequency: TransactionFrequency) {
        _uiState.value = _uiState.value.copy(draftFrequency = frequency)
    }

    private fun validateAndBuildDraft(isIncome: Boolean): DraftTransaction? {
        val state = uiState.value
        val amount = parseQtDouble(state.draftAmountInput)
        if (amount == null || amount < 0) {
            _uiState.value = state.copy(isDraftAmountValid = false, errorMessage = "Invalid amount")
            return null
        }
        val category = state.categories.find { it.id == state.draftCategoryId }
        if (category == null) {
            _uiState.value = state.copy(errorMessage = "Category required")
            return null
        }
        val date = parseShortDateToLocalDate(state.draftDateInput)
        if (date == null) {
            _uiState.value = state.copy(errorMessage = "Invalid date")
            return null
        }
        return DraftTransaction(
            localId = state.nextDraftId,
            isIncome = isIncome,
            amount = amount,
            categoryId = category.id,
            categoryName = category.name,
            description = state.draftDescriptionInput,
            date = date,
            frequency = state.draftFrequency,
        )
    }

    private fun resetDraftForm(state: NewAccountUiState): NewAccountUiState {
        return state.copy(
            draftAmountInput = "",
            isDraftAmountValid = true,
            draftCategoryId = 0,
            draftDescriptionInput = "",
            draftDateInput = formatDbDatetimeToShortDate(
                formatLocalDateToDbDatetime(LocalDate.now())
            ),
            draftFrequency = TransactionFrequency.MONTHLY,
            nextDraftId = state.nextDraftId + 1,
            errorMessage = null,
        )
    }

    fun addIncome() {
        val draft = validateAndBuildDraft(isIncome = true) ?: return
        _uiState.value = resetDraftForm(_uiState.value).copy(
            incomes = _uiState.value.incomes + draft
        )
    }

    fun addExpense() {
        val draft = validateAndBuildDraft(isIncome = false) ?: return
        _uiState.value = resetDraftForm(_uiState.value).copy(
            expenses = _uiState.value.expenses + draft
        )
    }

    fun removeIncome(localId: Int) {
        _uiState.value = _uiState.value.copy(
            incomes = _uiState.value.incomes.filterNot { it.localId == localId }
        )
    }

    fun removeExpense(localId: Int) {
        _uiState.value = _uiState.value.copy(
            expenses = _uiState.value.expenses.filterNot { it.localId == localId }
        )
    }

    private fun validateDetails(): Pair<String, Double>? {
        val state = uiState.value
        val name = state.nameInput
        if (name.isBlank()) {
            _uiState.value = state.copy(isNameValid = false, errorMessage = "Name cannot be empty")
            return null
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
            return null
        }
        return name to startingBalance
    }

    fun goToQuickSetup() {
        validateDetails() ?: return
        _uiState.value =
            _uiState.value.copy(step = NewAccountStep.RECURRING_INCOME, errorMessage = null)
    }

    fun goToExpenseStep() {
        _uiState.value =
            _uiState.value.copy(step = NewAccountStep.RECURRING_EXPENSE, errorMessage = null)
    }

    fun goToSummaryStep() {
        _uiState.value = _uiState.value.copy(step = NewAccountStep.SUMMARY, errorMessage = null)
    }

    fun previousStep() {
        val previous = when (_uiState.value.step) {
            NewAccountStep.DETAILS -> NewAccountStep.DETAILS
            NewAccountStep.RECURRING_INCOME -> NewAccountStep.DETAILS
            NewAccountStep.RECURRING_EXPENSE -> NewAccountStep.RECURRING_INCOME
            NewAccountStep.SUMMARY -> NewAccountStep.RECURRING_EXPENSE
        }
        _uiState.value = _uiState.value.copy(step = previous, errorMessage = null)
    }

    private suspend fun insertAccountAndMaybeSelect(name: String, startingBalance: Double): Int {
        val isFirstAccount = accountRepository.getAll().first().isEmpty()
        val id = accountRepository.insert(name, startingBalance).toInt()
        if (isFirstAccount) {
            accountRepository.selectAccount(id)
        }
        return id
    }

    fun saveAccount(onSuccess: () -> Unit) {
        val details = validateDetails() ?: return
        val (name, startingBalance) = details
        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                insertAccountAndMaybeSelect(name, startingBalance)
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

    fun finishQuickSetup(onSuccess: () -> Unit) {
        val details = validateDetails() ?: return
        val (name, startingBalance) = details
        val state = uiState.value
        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val accountId = insertAccountAndMaybeSelect(name, startingBalance)
                for (draft in state.incomes + state.expenses) {
                    val signedAmount = if (draft.isIncome) -draft.amount else draft.amount
                    transactionRepository.insert(
                        Transaction(
                            accountId = accountId,
                            amount = signedAmount,
                            categoryId = draft.categoryId,
                            description = draft.description,
                            createdAt = formatLocalDateToDbDatetime(draft.date),
                            frequency = draft.frequency.name,
                            recurrenceRootId = null,
                        )
                    )
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
