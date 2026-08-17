package com.juanitos.ui.routes.money.spendings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.FixedSpending
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import com.juanitos.lib.parseQtDouble
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditFixedSpendingUiState(
    val amountInput: String = "",
    val isAmountValid: Boolean = true,
    val categoryId: Int = 0,
    val descriptionInput: String = "",
    val dayOfMonthInput: String = "",
    val isDayOfMonthValid: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val categories: List<Category> = emptyList(),
    val original: FixedSpending? = null,
)

class EditFixedSpendingViewModel(
    savedStateHandle: SavedStateHandle,
    private val fixedSpendingRepository: FixedSpendingRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val fixedSpendingId: Int = checkNotNull(savedStateHandle["fixedSpendingId"])

    private val _uiState = MutableStateFlow(EditFixedSpendingUiState())
    val uiState: StateFlow<EditFixedSpendingUiState> =
        _uiState.combine(createCategoriesFlow()) { state, categories ->
            state.copy(categories = categories)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EditFixedSpendingUiState()
        )

    init {
        viewModelScope.launch {
            val existing = fixedSpendingRepository.getById(fixedSpendingId).first().fixedSpending
            _uiState.value = _uiState.value.copy(
                amountInput = existing.amount.toString(),
                categoryId = existing.categoryId,
                descriptionInput = existing.description ?: "",
                dayOfMonthInput = existing.dayOfMonth?.toString() ?: "",
                isLoading = false,
                original = existing,
            )
        }
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

    fun setCategoryInput(input: Int) {
        _uiState.value = _uiState.value.copy(categoryId = input, errorMessage = null)
    }

    fun setDescriptionInput(input: String) {
        _uiState.value = _uiState.value.copy(descriptionInput = input)
    }

    fun setDayOfMonthInput(input: String) {
        val day = input.toIntOrNull()
        _uiState.value = _uiState.value.copy(
            dayOfMonthInput = input,
            isDayOfMonthValid = input.isBlank() || (day != null && day in 1..31),
            errorMessage = null
        )
    }

    fun saveChanges(onSuccess: () -> Unit) {
        val state = _uiState.value
        val original = state.original ?: return
        val amount = parseQtDouble(state.amountInput)
        val category = state.categoryId
        val dayOfMonth = state.dayOfMonthInput.toIntOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(isAmountValid = false, errorMessage = "Invalid amount")
            return
        }
        if (category <= 0) {
            _uiState.value = state.copy(errorMessage = "Category required")
            return
        }
        if (state.dayOfMonthInput.isNotBlank() && (dayOfMonth == null || dayOfMonth !in 1..31)) {
            _uiState.value =
                state.copy(isDayOfMonthValid = false, errorMessage = "Invalid day of month")
            return
        }
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                fixedSpendingRepository.update(
                    original.copy(
                        amount = amount,
                        categoryId = category,
                        description = state.descriptionInput,
                        dayOfMonth = dayOfMonth,
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
