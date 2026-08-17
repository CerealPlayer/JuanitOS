package com.juanitos.ui.routes.money.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Cycle
import com.juanitos.data.money.entities.IncomeSchedule
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.IncomeScheduleRepository
import com.juanitos.lib.formatDbDatetimeToShortDate
import com.juanitos.lib.formatLocalDateToDbDatetime
import com.juanitos.lib.parseQtDouble
import com.juanitos.lib.parseShortDateToLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MoneySettingsUiState(
    val currentCycle: Cycle? = null,
    val schedule: IncomeSchedule? = null,

    val incomeInput: String = "",
    val isIncomeValid: Boolean = true,
    val isIncomeEdited: Boolean = false,

    val scheduleDayInput: String = "",
    val isScheduleDayValid: Boolean = true,
    val scheduleAmountInput: String = "",
    val isScheduleAmountValid: Boolean = true,
    val isScheduleEdited: Boolean = false,

    val editIncomeInput: String = "",
    val isEditIncomeValid: Boolean = true,
    val editStartDateInput: String = "",
    val isEditStartDateValid: Boolean = true,
    val isEditFormEdited: Boolean = false,

    val errorMessage: String? = null,
)

class MoneySettingsViewModel(
    private val cycleRepository: CycleRepository,
    private val incomeScheduleRepository: IncomeScheduleRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MoneySettingsUiState())
    val uiState: StateFlow<MoneySettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentCycle()
        loadSchedule()
    }

    private fun loadCurrentCycle() {
        viewModelScope.launch {
            cycleRepository.getCurrentCycle().collect { cycleWithDetails ->
                val cycle = cycleWithDetails?.cycle
                val current = _uiState.value
                _uiState.value = current.copy(
                    currentCycle = cycle,
                    editIncomeInput = if (current.isEditFormEdited) current.editIncomeInput
                    else cycle?.totalIncome?.toString() ?: "",
                    editStartDateInput = if (current.isEditFormEdited) current.editStartDateInput
                    else formatDbDatetimeToShortDate(cycle?.startDate)
                )
            }
        }
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            incomeScheduleRepository.get().collect { schedule ->
                val current = _uiState.value
                _uiState.value = current.copy(
                    schedule = schedule,
                    scheduleDayInput = if (current.isScheduleEdited) current.scheduleDayInput
                    else schedule?.dayOfMonth?.toString() ?: "",
                    scheduleAmountInput = if (current.isScheduleEdited) current.scheduleAmountInput
                    else schedule?.amount?.toString() ?: ""
                )
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
                onNavigateUp()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al crear ciclo")
            }
        }
    }

    fun endCurrentCycle() {
        val cycle = _uiState.value.currentCycle ?: return
        val schedule = _uiState.value.schedule
        viewModelScope.launch {
            try {
                cycleRepository.endCycle(cycle.id)
                // With a schedule active, reconciliation would silently reopen an equivalent
                // cycle on next app-open anyway -- do it immediately instead of one step delayed.
                if (schedule != null) {
                    cycleRepository.insert(schedule.amount)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al finalizar ciclo")
            }
        }
    }

    fun setScheduleDayInput(input: String) {
        val day = input.toIntOrNull()
        _uiState.value = _uiState.value.copy(
            scheduleDayInput = input,
            isScheduleDayValid = day != null && day in 1..31,
            isScheduleEdited = true,
            errorMessage = null
        )
    }

    fun setScheduleAmountInput(input: String) {
        _uiState.value = _uiState.value.copy(
            scheduleAmountInput = input,
            isScheduleAmountValid = parseQtDouble(input) != null,
            isScheduleEdited = true,
            errorMessage = null
        )
    }

    fun saveSchedule() {
        val state = _uiState.value
        val day = state.scheduleDayInput.toIntOrNull()
        val amount = parseQtDouble(state.scheduleAmountInput)
        if (day == null || day !in 1..31) {
            _uiState.value = state.copy(isScheduleDayValid = false, errorMessage = "Día inválido")
            return
        }
        if (amount == null || amount <= 0) {
            _uiState.value =
                state.copy(isScheduleAmountValid = false, errorMessage = "Ingreso inválido")
            return
        }
        viewModelScope.launch {
            try {
                incomeScheduleRepository.upsert(day, amount)
                _uiState.value = _uiState.value.copy(isScheduleEdited = false, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(errorMessage = "Error al guardar la programación")
            }
        }
    }

    fun setEditIncomeInput(input: String) {
        _uiState.value = _uiState.value.copy(
            editIncomeInput = input,
            isEditIncomeValid = parseQtDouble(input) != null,
            isEditFormEdited = true,
            errorMessage = null
        )
    }

    fun setEditStartDateInput(input: String) {
        _uiState.value = _uiState.value.copy(
            editStartDateInput = input,
            isEditStartDateValid = parseShortDateToLocalDate(input) != null,
            isEditFormEdited = true,
            errorMessage = null
        )
    }

    fun saveCycleEdits() {
        val state = _uiState.value
        val cycle = state.currentCycle ?: return
        val income = parseQtDouble(state.editIncomeInput)
        val startDate = parseShortDateToLocalDate(state.editStartDateInput)
        if (income == null || income <= 0) {
            _uiState.value =
                state.copy(isEditIncomeValid = false, errorMessage = "Ingreso inválido")
            return
        }
        if (startDate == null) {
            _uiState.value =
                state.copy(isEditStartDateValid = false, errorMessage = "Fecha inválida")
            return
        }
        viewModelScope.launch {
            try {
                cycleRepository.update(
                    cycle.copy(
                        totalIncome = income,
                        startDate = formatLocalDateToDbDatetime(startDate)
                    )
                )
                _uiState.value = _uiState.value.copy(isEditFormEdited = false, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al guardar los cambios")
            }
        }
    }
}
