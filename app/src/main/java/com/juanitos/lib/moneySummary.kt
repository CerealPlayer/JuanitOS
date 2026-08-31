package com.juanitos.lib

import com.juanitos.data.money.entities.relations.AccountWithDetails

data class CategoryExpenseSummary(
    val categoryName: String,
    val amount: Double,
)

data class MoneyAccountSummary(
    val totalIncome: Double,
    val categoryExpenses: List<CategoryExpenseSummary>,
    val totalExpenses: Double,
    val remaining: Double,
)

/**
 * Negative transaction amounts are treated as income-like adjustments (the same convention
 * used elsewhere, e.g. [com.juanitos.ui.routes.money.TransactionCard]'s accent color), so they
 * add to income rather than appearing as an expense category.
 */
fun computeAccountSummary(
    account: AccountWithDetails,
): MoneyAccountSummary {
    val incomeFromTransactions = account.transactions
        .filter { it.transaction.amount < 0 }
        .sumOf { -it.transaction.amount }
    val totalIncome = account.account.startingBalance + incomeFromTransactions

    val expensesByCategory = mutableMapOf<String, Double>()
    account.transactions
        .filter { it.transaction.amount > 0 }
        .forEach { transaction ->
            val categoryName = transaction.category?.name ?: "Uncategorized"
            expensesByCategory[categoryName] =
                (expensesByCategory[categoryName] ?: 0.0) + transaction.transaction.amount
        }
    val categoryExpenses = expensesByCategory
        .filter { it.value > 0.0 }
        .map { (categoryName, amount) -> CategoryExpenseSummary(categoryName, amount) }
        .sortedByDescending { it.amount }

    val totalExpenses = categoryExpenses.sumOf { it.amount }

    return MoneyAccountSummary(
        totalIncome = totalIncome,
        categoryExpenses = categoryExpenses,
        totalExpenses = totalExpenses,
        remaining = totalIncome - totalExpenses,
    )
}
