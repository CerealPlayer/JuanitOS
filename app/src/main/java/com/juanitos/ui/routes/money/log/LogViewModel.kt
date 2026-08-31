package com.juanitos.ui.routes.money.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.entities.relations.AccountWithDetails
import com.juanitos.data.money.entities.relations.TransactionWithCategory
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.TransactionRepository
import com.juanitos.lib.parseDbDatetimeToLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LogUiState(
    val transactions: List<TransactionWithCategory> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
)

class LogViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val selectedCategoryId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<LogUiState> = combine(
        createAccountFlow(),
        categoryRepository.getAll(),
        searchQuery,
        selectedCategoryId,
    ) { account, categories, query, categoryId ->
        val transactions = sortedTransactions(account)
            .filter { matchesQuery(it, query) && matchesCategory(it, categoryId) }
        LogUiState(
            transactions = transactions,
            categories = categories,
            searchQuery = query,
            selectedCategory = categories.find { it.id == categoryId },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = LogUiState()
    )

    private fun createAccountFlow(): Flow<AccountWithDetails?> {
        return accountRepository.getSelected()
    }

    private fun sortedTransactions(account: AccountWithDetails?): List<TransactionWithCategory> {
        return (account?.transactions ?: emptyList()).sortedWith(
            compareBy(
                { parseDbDatetimeToLocalDate(it.transaction.createdAt) },
                { it.transaction.id }
            )
        )
    }

    private fun matchesQuery(transaction: TransactionWithCategory, query: String): Boolean {
        if (query.isBlank()) return true
        return transaction.transaction.description?.contains(query, ignoreCase = true) ?: false
    }

    private fun matchesCategory(transaction: TransactionWithCategory, categoryId: Int?): Boolean {
        if (categoryId == null) return true
        return transaction.category?.id == categoryId
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
