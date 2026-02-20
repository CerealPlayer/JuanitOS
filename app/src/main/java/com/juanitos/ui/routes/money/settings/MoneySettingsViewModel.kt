package com.juanitos.ui.routes.money.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Cycle
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.lib.parseQtDouble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MoneySettingsUiState(
    val incomeInput: String = "",
    val isIncomeValid: Boolean = true,
    val isIncomeEdited: Boolean = false,
    val errorMessage: String? = null,
    val currentCycle: Cycle? = null
)

class MoneySettingsViewModel(
    private val cycleRepository: CycleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MoneySettingsUiState())
    val uiState: StateFlow<MoneySettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentCycle()
    }

    private fun loadCurrentCycle() {
        viewModelScope.launch {
            cycleRepository.getCurrentCycle().collect { cycleWithDetails ->
                _uiState.value = _uiState.value.copy(currentCycle = cycleWithDetails?.cycle)
            }
        }
    }

    fun setIncomeInput(input: String) {
        _uiState.value = _uiState.value.copy(
            incomeInput = input,
            isIncomeValid = parseQtDouble(input) != null,
            isIncomeEdited = true,
            errorMessage = null
        )
    }

    fun createNewCycle(onNavigateUp: () -> Unit) {
        val input = _uiState.value.incomeInput
        val income = parseQtDouble(input)
        if (income == null || income <= 0) {
            _uiState.value =
                _uiState.value.copy(isIncomeValid = false, errorMessage = "Ingreso inválido")
            return
        }
        viewModelScope.launch {
            try {
                cycleRepository.insert(income)
                _uiState.value = _uiState.value.copy(
                    incomeInput = "",
                    isIncomeValid = true,
                    isIncomeEdited = false,
                    errorMessage = null
                )
                loadCurrentCycle()
                onNavigateUp()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al crear ciclo")
            }
        }
    }

    fun endCurrentCycle() {
        val cycle = _uiState.value.currentCycle ?: return
        viewModelScope.launch {
            try {
                cycleRepository.endCycle(cycle.id)
                loadCurrentCycle()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al finalizar ciclo")
            }
        }
    }
}
