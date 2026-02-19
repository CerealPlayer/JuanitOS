package com.juanitos.ui.routes.money.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.repositories.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
)

class CategoriesViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState = _uiState.combine(createCategoriesFlow()) { state, categories ->
        state.copy(categories = categories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoriesUiState()
    )

    private fun createCategoriesFlow(): Flow<List<Category>> {
        return categoryRepository.getAll()
    }
}

