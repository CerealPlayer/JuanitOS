package com.juanitos.testing

import com.juanitos.data.money.entities.Transaction
import com.juanitos.lib.formatLocalDateToDbDatetime
import java.time.LocalDate

/**
 * Precondition helpers that write directly through a [TestAppContainer]'s real repositories,
 * so tests can set up state without driving the UI for unrelated setup steps.
 */
object TestSeed {
    /** Inserts an account and selects it, returning its id. */
    suspend fun account(
        container: TestAppContainer,
        name: String = "Main",
        startingBalance: Double = 0.0,
    ): Int {
        val id = container.accountRepository.insert(name, startingBalance).toInt()
        container.accountRepository.selectAccount(id)
        return id
    }

    /** Inserts a category, returning its id. */
    suspend fun category(
        container: TestAppContainer,
        name: String = "Groceries",
        description: String? = null,
    ): Int = container.categoryRepository.insert(name, description).toInt()

    /** Inserts a transaction on the given date (defaults to today), returning its id. */
    suspend fun transaction(
        container: TestAppContainer,
        accountId: Int,
        categoryId: Int,
        amount: Double,
        description: String? = null,
        date: LocalDate = LocalDate.now(),
        frequency: String? = null,
    ): Int = container.transactionRepository.insert(
        Transaction(
            accountId = accountId,
            amount = amount,
            categoryId = categoryId,
            description = description,
            createdAt = formatLocalDateToDbDatetime(date),
            frequency = frequency,
        )
    ).toInt()
}
