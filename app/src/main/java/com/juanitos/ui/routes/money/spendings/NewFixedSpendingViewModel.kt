package com.juanitos.ui.routes.money.spendings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.FixedSpending
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

// Estado de la UI para el formulario de gasto fijo
data class NewFixedSpendingUiState(
    val amountInput: String = "",
    val isAmountValid: Boolean = true,
    val categoryInput: String = "",
    val descriptionInput: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val currentCycleId: Int? = null
)

class NewFixedSpendingViewModel(
    private val fixedSpendingRepository: FixedSpendingRepository,
    private val cycleRepository: CycleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewFixedSpendingUiState())
    val uiState: StateFlow<NewFixedSpendingUiState> = _uiState.asStateFlow()

    init {
        loadCurrentCycleId()
    }

    private fun loadCurrentCycleId() {
        viewModelScope.launch {
            val cycle = cycleRepository.getCurrentCycle().firstOrNull()?.cycle
            _uiState.value = _uiState.value.copy(currentCycleId = cycle?.id)
        }
    }

    fun setAmountInput(input: String) {
        _uiState.value = _uiState.value.copy(
            amountInput = input,
            isAmountValid = input.toDoubleOrNull() != null && input.toDouble() > 0,
            errorMessage = null
        )
    }

    fun setCategoryInput(input: String) {
        _uiState.value = _uiState.value.copy(categoryInput = input, errorMessage = null)
    }

    fun setDescriptionInput(input: String) {
        _uiState.value = _uiState.value.copy(descriptionInput = input)
    }

    fun saveFixedSpending(onSuccess: () -> Unit) {
        val state = _uiState.value
        val amount = state.amountInput.toDoubleOrNull()
        val category = state.categoryInput.trim()
        val cycleId = state.currentCycleId
        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(isAmountValid = false, errorMessage = "Invalid amount")
            return
        }
        if (category.isEmpty()) {
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
                val fixedSpending = FixedSpending(
                    cycleId = cycleId,
                    amount = amount,
                    category = category,
                    description = state.descriptionInput.takeIf { it.isNotBlank() }
                )
                fixedSpendingRepository.insert(fixedSpending)
                _uiState.value = state.copy(success = true, isSaving = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Error saving spending"
                )
            }
        }
    }
}

