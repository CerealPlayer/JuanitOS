package com.juanitos.lib

import com.juanitos.data.money.entities.Account
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.entities.relations.AccountWithDetails
import com.juanitos.data.money.entities.relations.TransactionWithCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneySummaryTest {

    private val food = Category(id = 1, name = "Food")
    private val rent = Category(id = 2, name = "Rent")

    private fun accountWith(
        startingBalance: Double = 2000.0,
        transactions: List<TransactionWithCategory> = emptyList(),
    ) = AccountWithDetails(
        account = Account(id = 1, name = "Main", startingBalance = startingBalance),
        transactions = transactions,
    )

    private fun transaction(amount: Double, category: Category?) = TransactionWithCategory(
        transaction = Transaction(accountId = 1, amount = amount, categoryId = category?.id ?: 0),
        category = category,
    )

    @Test
    fun noTransactions_incomeOnlyRemaining() {
        val summary = computeAccountSummary(accountWith(startingBalance = 1500.0))

        assertEquals(1500.0, summary.totalIncome, 0.0)
        assertEquals(emptyList<CategoryExpenseSummary>(), summary.categoryExpenses)
        assertEquals(0.0, summary.totalExpenses, 0.0)
        assertEquals(1500.0, summary.remaining, 0.0)
    }

    @Test
    fun transactions_groupedByCategory() {
        val summary = computeAccountSummary(
            accountWith(
                startingBalance = 1000.0,
                transactions = listOf(transaction(400.0, rent))
            )
        )

        assertEquals(
            listOf(CategoryExpenseSummary("Rent", 400.0)),
            summary.categoryExpenses
        )
        assertEquals(400.0, summary.totalExpenses, 0.0)
        assertEquals(600.0, summary.remaining, 0.0)
    }

    @Test
    fun multipleTransactions_mergeIntoSameCategory() {
        val summary = computeAccountSummary(
            accountWith(
                startingBalance = 2000.0,
                transactions = listOf(
                    transaction(50.0, food),
                    transaction(100.0, rent),
                    transaction(400.0, rent)
                )
            )
        )

        val byCategory = summary.categoryExpenses.associate { it.categoryName to it.amount }
        assertEquals(50.0, byCategory["Food"]!!, 0.0)
        assertEquals(500.0, byCategory["Rent"]!!, 0.0)
        assertEquals(550.0, summary.totalExpenses, 0.0)
        assertEquals(1450.0, summary.remaining, 0.0)
    }

    @Test
    fun negativeTransaction_addsToIncomeInsteadOfExpenses() {
        val summary = computeAccountSummary(
            accountWith(
                startingBalance = 2000.0,
                transactions = listOf(transaction(-100.0, food), transaction(30.0, food))
            )
        )

        assertEquals(2100.0, summary.totalIncome, 0.0)
        assertEquals(
            listOf(CategoryExpenseSummary("Food", 30.0)),
            summary.categoryExpenses
        )
        assertEquals(2070.0, summary.remaining, 0.0)
    }

    @Test
    fun zeroAmountTransaction_isHidden() {
        val summary = computeAccountSummary(
            accountWith(
                startingBalance = 1000.0,
                transactions = listOf(transaction(0.0, rent))
            )
        )

        assertEquals(emptyList<CategoryExpenseSummary>(), summary.categoryExpenses)
    }

    @Test
    fun uncategorizedTransaction_groupedUnderUncategorized() {
        val summary = computeAccountSummary(
            accountWith(transactions = listOf(transaction(75.0, null)))
        )

        assertEquals(
            listOf(CategoryExpenseSummary("Uncategorized", 75.0)),
            summary.categoryExpenses
        )
    }
}
