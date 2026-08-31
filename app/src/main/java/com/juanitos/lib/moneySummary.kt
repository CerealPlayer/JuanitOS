package com.juanitos.lib

import com.juanitos.data.money.entities.relations.AccountWithDetails
import kotlin.math.abs

data class CategoryAmountSummary(
    val categoryName: String,
    val amount: Double,
    val isIncome: Boolean,
)

data class MoneyAccountSummary(
    val accountName: String,
    val remaining: Double,
    val categorySummaries: List<CategoryAmountSummary>,
)

/**
 * Negative transaction amounts represent income, positive amounts represent expenses (the same
 * convention used elsewhere, e.g. [com.juanitos.ui.routes.money.TransactionCard]'s accent color).
 * Income is just another kind of transaction, so it is broken down by category exactly like
 * expenses rather than being collapsed into a single total.
 */
fun computeAccountSummary(
    account: AccountWithDetails,
): MoneyAccountSummary {
    val appliedTransactions =
        account.transactions.filterNot {
            isPendingTransaction(it.transaction.createdAt) || it.transaction.creditCardId != null
        }

    val categorySummaries = appliedTransactions
        .groupBy { (it.category?.name ?: "Uncategorized") to (it.transaction.amount < 0) }
        .map { (key, transactions) ->
            val (categoryName, isIncome) = key
            CategoryAmountSummary(
                categoryName = categoryName,
                amount = transactions.sumOf { abs(it.transaction.amount) },
                isIncome = isIncome,
            )
        }
        .filter { it.amount > 0.0 }
        .sortedByDescending { it.amount }

    val totalIncome = account.account.startingBalance +
            categorySummaries.filter { it.isIncome }.sumOf { it.amount }
    val totalExpenses = categorySummaries.filterNot { it.isIncome }.sumOf { it.amount }

    return MoneyAccountSummary(
        accountName = account.account.name,
        remaining = totalIncome - totalExpenses,
        categorySummaries = categorySummaries,
    )
}
