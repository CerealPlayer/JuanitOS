package com.juanitos.ui.routes.money.spendings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import com.juanitos.data.money.repositories.FixedSpendingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FixedSpendingsUiState(
    val fixedSpendings: List<FixedSpendingWithCategory> = emptyList(),
)

class FixedSpendingsViewModel(private val fixedSpendingRepository: FixedSpendingRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(FixedSpendingsUiState())
    val uiState = _uiState.combine(createFixedSpendingsFlow()) { state, fixedSpendings ->
        state.copy(fixedSpendings = fixedSpendings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FixedSpendingsUiState()
    )

    private fun createFixedSpendingsFlow(): Flow<List<FixedSpendingWithCategory>> {
        return fixedSpendingRepository.getAll()
    }

    fun toggleFixedSpendingEnabled(spendingId: Int, enabled: Boolean) {
        viewModelScope.launch {
            fixedSpendingRepository.updateEnabled(spendingId, enabled)
        }
    }
}