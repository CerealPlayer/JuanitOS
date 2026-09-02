package com.juanitos.lib

import com.juanitos.data.money.entities.Account
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.entities.relations.AccountWithDetails
import com.juanitos.data.money.entities.relations.TransactionWithCategory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MoneySummaryTest {

    private val food = Category(id = 1, name = "Food")
    private val rent = Category(id = 2, name = "Rent")
    private val salary = Category(id = 3, name = "Salary")

    private fun accountWith(
        startingBalance: Double = 2000.0,
        currentBalance: Double,
        transactions: List<TransactionWithCategory> = emptyList(),
    ) = AccountWithDetails(
        account = Account(
            id = 1,
            name = "Main",
            startingBalance = startingBalance,
            currentBalance = currentBalance,
        ),
        transactions = transactions,
    )

    private fun transaction(
        amount: Double,
        category: Category?,
        createdAt: String? = null,
    ) = TransactionWithCategory(
        transaction = Transaction(
            accountId = 1,
            amount = amount,
            categoryId = category?.id ?: 0,
            createdAt = createdAt,
        ),
        category = category,
    )

    @Test
    fun noTransactions_startingBalanceOnlyRemaining() {
        val summary =
            computeAccountSummary(accountWith(startingBalance = 1500.0, currentBalance = 1500.0))

        assertEquals(emptyList<CategoryAmountSummary>(), summary.categorySummaries)
        assertEquals(1500.0, summary.remaining, 0.0)
    }

    @Test
    fun transactions_groupedByCategory() {
        val summary = computeAccountSummary(
            accountWith(
                startingBalance = 1000.0,
                currentBalance = 600.0,
                transactions = listOf(transaction(400.0, rent))
            )
        )

        assertEquals(
            listOf(CategoryAmountSummary("Rent", 400.0, isIncome = false)),
            summary.categorySummaries
        )
        assertEquals(600.0, summary.remaining, 0.0)
    }

    @Test
    fun multipleTransactions_mergeIntoSameCategory() {
        val summary = computeAccountSummary(
            accountWith(
                startingBalance = 2000.0,
                currentBalance = 1450.0,
                transactions = listOf(
                    transaction(50.0, food),
                    transaction(100.0, rent),
                    transaction(400.0, rent)
                )
            )
        )

        val byCategory = summary.categorySummaries.associate { it.categoryName to it.amount }
        assertEquals(50.0, byCategory["Food"]!!, 0.0)
        assertEquals(500.0, byCategory["Rent"]!!, 0.0)
        assertEquals(1450.0, summary.remaining, 0.0)
    }

    @Test
    fun negativeTransaction_isItsOwnIncomeCategory() {
        val summary = computeAccountSummary(
            accountWith(
                startingBalance = 2000.0,
                currentBalance = 2070.0,
                transactions = listOf(transaction(-100.0, salary), transaction(30.0, food))
            )
        )

        assertEquals(
            setOf(
                CategoryAmountSummary("Salary", 100.0, isIncome = true),
                CategoryAmountSummary("Food", 30.0, isIncome = false),
            ),
            summary.categorySummaries.toSet()
        )
        assertEquals(2070.0, summary.remaining, 0.0)
    }

    @Test
    fun sameCategory_incomeAndExpenseKeptSeparate() {
        val summary = computeAccountSummary(
            accountWith(
                startingBalance = 1000.0,
                currentBalance = 1020.0,
                transactions = listOf(transaction(-50.0, food), transaction(30.0, food))
            )
        )

        assertEquals(
            setOf(
                CategoryAmountSummary("Food", 50.0, isIncome = true),
                CategoryAmountSummary("Food", 30.0, isIncome = false),
            ),
            summary.categorySummaries.toSet()
        )
        assertEquals(1020.0, summary.remaining, 0.0)
    }

    @Test
    fun zeroAmountTransaction_isHidden() {
        val summary = computeAccountSummary(
            accountWith(
                startingBalance = 1000.0,
                currentBalance = 1000.0,
                transactions = listOf(transaction(0.0, rent))
            )
        )

        assertEquals(emptyList<CategoryAmountSummary>(), summary.categorySummaries)
    }

    @Test
    fun uncategorizedTransaction_groupedUnderUncategorized() {
        val summary = computeAccountSummary(
            accountWith(currentBalance = 1925.0, transactions = listOf(transaction(75.0, null)))
        )

        assertEquals(
            listOf(CategoryAmountSummary("Uncategorized", 75.0, isIncome = false)),
            summary.categorySummaries
        )
    }

    @Test
    fun futureDatedTransaction_excludedFromTotals() {
        val futureDate = LocalDate.now().plusDays(5).toString() + " 00:00:00"
        val summary = computeAccountSummary(
            accountWith(
                startingBalance = 1000.0,
                currentBalance = 900.0,
                transactions = listOf(
                    transaction(100.0, rent),
                    transaction(400.0, rent, createdAt = futureDate)
                )
            )
        )

        assertEquals(
            listOf(CategoryAmountSummary("Rent", 100.0, isIncome = false)),
            summary.categorySummaries
        )
        assertEquals(900.0, summary.remaining, 0.0)
    }
}
