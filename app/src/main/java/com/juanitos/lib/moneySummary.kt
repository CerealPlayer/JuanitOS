package com.juanitos.lib

import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory

data class CategoryExpenseSummary(
    val categoryName: String,
    val amount: Double,
)

data class MoneyCycleSummary(
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
fun computeMoneyCycleSummary(
    cycle: CurrentCycleWithDetails,
    fixedSpendings: List<FixedSpendingWithCategory>,
): MoneyCycleSummary {
    val incomeFromTransactions = cycle.transactions
        .filter { it.transaction.amount < 0 }
        .sumOf { -it.transaction.amount }
    val totalIncome = cycle.cycle.totalIncome + incomeFromTransactions

    val expensesByCategory = mutableMapOf<String, Double>()
    cycle.transactions
        .filter { it.transaction.amount > 0 }
        .forEach { transaction ->
            val categoryName = transaction.category?.name ?: "Uncategorized"
            expensesByCategory[categoryName] =
                (expensesByCategory[categoryName] ?: 0.0) + transaction.transaction.amount
        }
    fixedSpendings.forEach { fixedSpending ->
        val categoryName = fixedSpending.category.name
        expensesByCategory[categoryName] =
            (expensesByCategory[categoryName] ?: 0.0) + fixedSpending.fixedSpending.amount
    }

    val categoryExpenses = expensesByCategory
        .filter { it.value > 0.0 }
        .map { (categoryName, amount) -> CategoryExpenseSummary(categoryName, amount) }
        .sortedByDescending { it.amount }

    val totalExpenses = categoryExpenses.sumOf { it.amount }

    return MoneyCycleSummary(
        totalIncome = totalIncome,
        categoryExpenses = categoryExpenses,
        totalExpenses = totalExpenses,
        remaining = totalIncome - totalExpenses,
    )
}
