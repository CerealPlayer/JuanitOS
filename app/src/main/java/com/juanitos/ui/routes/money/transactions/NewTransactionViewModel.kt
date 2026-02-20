package com.juanitos.ui.routes.money.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.TransactionRepository
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
    val currentCycleId: Int? = null,
    val categories: List<Category> = emptyList()
)

class NewTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val cycleRepository: CycleRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewTransactionUiState())
    val uiState: StateFlow<NewTransactionUiState> =
        _uiState.combine(createCurrentCycleFlow()) { state, cycle ->
            state.copy(currentCycleId = cycle?.cycle?.id)
        }.combine(createCategoriesFlow()) { state, categories ->
            state.copy(categories = categories)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NewTransactionUiState()
        )

    private fun createCurrentCycleFlow(): Flow<CurrentCycleWithDetails?> {
        return cycleRepository.getCurrentCycle()
    }

    private fun createCategoriesFlow(): Flow<List<Category>> {
        return categoryRepository.getAll()
    }

    fun setAmountInput(input: String) {
        _uiState.value = _uiState.value.copy(
            amountInput = input,
            isAmountValid = input.toDoubleOrNull() != null && input.toDouble() > 0,
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
        val amount = state.amountInput.toDoubleOrNull()
        val category = state.categoryId
        val cycleId = state.currentCycleId
        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(isAmountValid = false, errorMessage = "Invalid amount")
            return
        }
        if (category <= 0) {
            _uiState.value = state.copy(errorMessage = "Category required")
            return
        }
        if (cycleId == null) {
            _uiState.value = state.copy(errorMessage = "No active cycle")
            return
        }
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                transactionRepository.insert(
                    Transaction(
                        cycleId = cycleId,
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
