package com.juanitos.ui.routes.money.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.SavingsGoal
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.SavingsGoalRepository
import com.juanitos.lib.GoalType
import com.juanitos.lib.parseQtDouble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

data class SavingsGoalUiState(
    val accountId: Int? = null,
    val hasExistingGoal: Boolean = false,
    val goalType: GoalType = GoalType.FIXED,
    val fixedAmountInput: String = "",
    val isFixedAmountValid: Boolean = true,
    val percentageInput: String = "",
    val isPercentageValid: Boolean = true,
    val notificationsEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
)

class SavingsGoalViewModel(
    private val accountRepository: AccountRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SavingsGoalUiState())
    val uiState: StateFlow<SavingsGoalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val accountId = accountRepository.getSelected().first()?.account?.id ?: return@launch
            val existing = savingsGoalRepository.getByAccountOnce(accountId)
            _uiState.value = if (existing == null) {
                SavingsGoalUiState(accountId = accountId)
            } else {
                SavingsGoalUiState(
                    accountId = accountId,
                    hasExistingGoal = true,
                    goalType = GoalType.valueOf(existing.goalType),
                    fixedAmountInput = existing.fixedAmount?.let(::formatInputAmount) ?: "",
                    percentageInput = existing.percentage?.let(::formatInputAmount) ?: "",
                    notificationsEnabled = existing.notificationsEnabled,
                )
            }
        }
    }

    fun setGoalType(type: GoalType) {
        _uiState.value = _uiState.value.copy(goalType = type, errorMessage = null)
    }

    fun setFixedAmountInput(input: String) {
        _uiState.value = _uiState.value.copy(
            fixedAmountInput = input,
            isFixedAmountValid = true,
            errorMessage = null,
        )
    }

    fun setPercentageInput(input: String) {
        _uiState.value = _uiState.value.copy(
            percentageInput = input,
            isPercentageValid = true,
            errorMessage = null,
        )
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun saveGoal(onSuccess: () -> Unit) {
        val state = _uiState.value
        val accountId = state.accountId ?: return

        val fixedAmount: Double?
        val percentage: Double?
        when (state.goalType) {
            GoalType.FIXED -> {
                val amount = parseQtDouble(state.fixedAmountInput)
                if (amount == null || amount <= 0.0) {
                    _uiState.value = state.copy(
                        isFixedAmountValid = false,
                        errorMessage = "Enter a valid amount"
                    )
                    return
                }
                fixedAmount = amount
                percentage = null
            }

            GoalType.PERCENTAGE -> {
                val pct = parseQtDouble(state.percentageInput)
                if (pct == null || pct <= 0.0 || pct > 100.0) {
                    _uiState.value = state.copy(
                        isPercentageValid = false,
                        errorMessage = "Enter a percentage between 0 and 100"
                    )
                    return
                }
                fixedAmount = null
                percentage = pct
            }
        }

        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                savingsGoalRepository.upsert(
                    SavingsGoal(
                        accountId = accountId,
                        goalType = state.goalType.name,
                        fixedAmount = fixedAmount,
                        percentage = percentage,
                        notificationsEnabled = state.notificationsEnabled,
                    )
                )
                _uiState.value = _uiState.value.copy(isSaving = false, success = true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Error saving goal",
                )
            }
        }
    }
}

private fun formatInputAmount(value: Double): String = String.format(Locale.US, "%.2f", value)
