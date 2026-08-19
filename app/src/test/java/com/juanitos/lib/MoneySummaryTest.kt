package com.juanitos.lib

import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.Cycle
import com.juanitos.data.money.entities.FixedSpending
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import com.juanitos.data.money.entities.relations.TransactionWithCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneySummaryTest {

    private val food = Category(id = 1, name = "Food")
    private val rent = Category(id = 2, name = "Rent")

    private fun cycleWith(
        totalIncome: Double = 2000.0,
        transactions: List<TransactionWithCategory> = emptyList(),
    ) = CurrentCycleWithDetails(
        cycle = Cycle(id = 1, totalIncome = totalIncome),
        transactions = transactions,
    )

    private fun transaction(amount: Double, category: Category?) = TransactionWithCategory(
        transaction = Transaction(cycleId = 1, amount = amount, categoryId = category?.id ?: 0),
        category = category,
    )

    private fun fixedSpending(amount: Double, category: Category) = FixedSpendingWithCategory(
        fixedSpending = FixedSpending(amount = amount, categoryId = category.id),
        category = category,
    )

    @Test
    fun noTransactionsOrFixedSpendings_incomeOnlyRemaining() {
        val summary = computeMoneyCycleSummary(cycleWith(totalIncome = 1500.0), emptyList())

        assertEquals(1500.0, summary.totalIncome, 0.0)
        assertEquals(emptyList<CategoryExpenseSummary>(), summary.categoryExpenses)
        assertEquals(0.0, summary.totalExpenses, 0.0)
        assertEquals(1500.0, summary.remaining, 0.0)
    }

    @Test
    fun onlyFixedSpendings_groupedByCategory() {
        val summary = computeMoneyCycleSummary(
            cycleWith(totalIncome = 1000.0),
            listOf(fixedSpending(400.0, rent))
        )

        assertEquals(
            listOf(CategoryExpenseSummary("Rent", 400.0)),
            summary.categoryExpenses
        )
        assertEquals(400.0, summary.totalExpenses, 0.0)
        assertEquals(600.0, summary.remaining, 0.0)
    }

    @Test
    fun mixedTransactionsAndFixedSpendings_mergeIntoSameCategory() {
        val summary = computeMoneyCycleSummary(
            cycleWith(
                totalIncome = 2000.0,
                transactions = listOf(transaction(50.0, food), transaction(100.0, rent))
            ),
            listOf(fixedSpending(400.0, rent))
        )

        val byCategory = summary.categoryExpenses.associate { it.categoryName to it.amount }
        assertEquals(50.0, byCategory["Food"]!!, 0.0)
        assertEquals(500.0, byCategory["Rent"]!!, 0.0)
        assertEquals(550.0, summary.totalExpenses, 0.0)
        assertEquals(1450.0, summary.remaining, 0.0)
    }

    @Test
    fun negativeTransaction_addsToIncomeInsteadOfExpenses() {
        val summary = computeMoneyCycleSummary(
            cycleWith(
                totalIncome = 2000.0,
                transactions = listOf(transaction(-100.0, food), transaction(30.0, food))
            ),
            emptyList()
        )

        assertEquals(2100.0, summary.totalIncome, 0.0)
        assertEquals(
            listOf(CategoryExpenseSummary("Food", 30.0)),
            summary.categoryExpenses
        )
        assertEquals(2070.0, summary.remaining, 0.0)
    }

    @Test
    fun zeroAmountFixedSpending_isHidden() {
        val summary = computeMoneyCycleSummary(
            cycleWith(totalIncome = 1000.0),
            listOf(fixedSpending(0.0, rent))
        )

        assertEquals(emptyList<CategoryExpenseSummary>(), summary.categoryExpenses)
    }

    @Test
    fun uncategorizedTransaction_groupedUnderUncategorized() {
        val summary = computeMoneyCycleSummary(
            cycleWith(transactions = listOf(transaction(75.0, null))),
            emptyList()
        )

        assertEquals(
            listOf(CategoryExpenseSummary("Uncategorized", 75.0)),
            summary.categoryExpenses
        )
    }
}
