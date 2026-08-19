package com.juanitos.ui.routes.money.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import com.juanitos.data.money.repositories.TransactionRepository
import com.juanitos.lib.clampDayOfMonth
import com.juanitos.lib.parseDbDatetimeToLocalDate
import com.juanitos.ui.routes.money.Movement
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class LogUiState(
    val movements: List<Movement> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModel(
    private val cycleRepository: CycleRepository,
    private val fixedSpendingRepository: FixedSpendingRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val selectedCategoryId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<LogUiState> = combine(
        createCycleFlow(),
        createFixedSpendingsFlow(),
        categoryRepository.getAll(),
        searchQuery,
        selectedCategoryId,
    ) { cycle, fixedSpendings, categories, query, categoryId ->
        val movements = mergeMovements(cycle, fixedSpendings)
            .filter { matchesQuery(it, query) && matchesCategory(it, categoryId) }
        LogUiState(
            movements = movements,
            categories = categories,
            searchQuery = query,
            selectedCategory = categories.find { it.id == categoryId },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = LogUiState()
    )

    private fun createCycleFlow(): Flow<CurrentCycleWithDetails?> {
        return cycleRepository.getCurrentCycle()
    }

    private fun createFixedSpendingsFlow(): Flow<List<FixedSpendingWithCategory>> {
        return fixedSpendingRepository.getAll().map {
            it.filter { s -> s.fixedSpending.active }
        }
    }

    private fun matchesQuery(movement: Movement, query: String): Boolean {
        if (query.isBlank()) return true
        val description = when (movement) {
            is Movement.FixedSpendingMovement -> movement.fixedSpending.fixedSpending.description
            is Movement.TransactionMovement -> movement.transaction.transaction.description
        }
        return description?.contains(query, ignoreCase = true) ?: false
    }

    private fun matchesCategory(movement: Movement, categoryId: Int?): Boolean {
        if (categoryId == null) return true
        val movementCategoryId = when (movement) {
            is Movement.FixedSpendingMovement -> movement.fixedSpending.category.id
            is Movement.TransactionMovement -> movement.transaction.category?.id
        }
        return movementCategoryId == categoryId
    }

    private fun mergeMovements(
        cycle: CurrentCycleWithDetails?,
        fixedSpendings: List<FixedSpendingWithCategory>
    ): List<Movement> {
        val cycleMonth = cycle?.cycle?.startDate
            ?.let(::parseDbDatetimeToLocalDate)
            ?.let(YearMonth::from)
            ?: YearMonth.now()

        val fixedSpendingMovements = fixedSpendings.map { fixedSpending ->
            Movement.FixedSpendingMovement(
                fixedSpending = fixedSpending,
                date = fixedSpending.fixedSpending.dayOfMonth?.let {
                    clampDayOfMonth(
                        cycleMonth,
                        it
                    )
                }
            )
        }
        val transactionMovements = (cycle?.transactions ?: emptyList())
            .map { Movement.TransactionMovement(it) }

        // Undated fixed spendings sort first (matches pre-scheduling behavior), then everything
        // else interleaved by date ascending, ties broken fixed-spending-before-transaction.
        return (fixedSpendingMovements + transactionMovements).sortedWith(
            compareBy(
                { it.date != null },
                { it.date },
                { movementTypePriority(it) },
                { it.sortId }
            )
        )
    }

    private fun movementTypePriority(movement: Movement): Int = when (movement) {
        is Movement.FixedSpendingMovement -> 0
        is Movement.TransactionMovement -> 1
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setCategoryFilter(category: Category?) {
        selectedCategoryId.value = category?.id
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.delete(transaction)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
