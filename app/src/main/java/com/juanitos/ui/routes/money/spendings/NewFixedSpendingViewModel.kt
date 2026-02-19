package com.juanitos.ui.routes.money.spendings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.FixedSpending
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Estado de la UI para el formulario de gasto fijo
data class NewFixedSpendingUiState(
    val amountInput: String = "",
    val isAmountValid: Boolean = true,
    val categoryId: Int = 0,
    val descriptionInput: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val categories: List<Category> = emptyList(),
)

class NewFixedSpendingViewModel(
    private val fixedSpendingRepository: FixedSpendingRepository,
    private val categoriesRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewFixedSpendingUiState())
    val uiState: StateFlow<NewFixedSpendingUiState> =
        _uiState.combine(createCategoriesFlow()) { state, categories ->
            state.copy(categories = categories)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NewFixedSpendingUiState()
        )

    fun createCategoriesFlow(): Flow<List<Category>> {
        return categoriesRepository.getAll()
    }

    fun setAmountInput(input: String) {
        _uiState.value = _uiState.value.copy(
            amountInput = input,
            isAmountValid = input.toDoubleOrNull() != null && input.toDouble() > 0,
            errorMessage = null
        )
    }

    fun setCategoryInput(input: Int) {
        _uiState.value = _uiState.value.copy(categoryId = input, errorMessage = null)
    }

    fun setDescriptionInput(input: String) {
        _uiState.value = _uiState.value.copy(descriptionInput = input)
    }

    fun saveFixedSpending(onSuccess: () -> Unit) {
        val state = _uiState.value
        val amount = state.amountInput.toDoubleOrNull()
        val category = state.categoryId
        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(isAmountValid = false, errorMessage = "Invalid amount")
            return
        }
        if (category <= 0) {
            _uiState.value = state.copy(errorMessage = "Category required")
            return
        }
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                fixedSpendingRepository.insert(
                    FixedSpending(
                        amount = amount,
                        categoryId = category,
                        description = state.descriptionInput,
                    )
                )
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
