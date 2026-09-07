package com.juanitos.testing

import android.content.Context
import com.juanitos.data.AppContainer
import com.juanitos.data.JuanitOSDatabase
import com.juanitos.data.money.offline.OfflineAccountRepository
import com.juanitos.data.money.offline.OfflineCategoryRepository
import com.juanitos.data.money.offline.OfflineCreditCardRepository
import com.juanitos.data.money.offline.OfflineMonthlySummaryRepository
import com.juanitos.data.money.offline.OfflineSavingsGoalRepository
import com.juanitos.data.money.offline.OfflineTransactionRepository
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.CreditCardRepository
import com.juanitos.data.money.repositories.MonthlySummaryRepository
import com.juanitos.data.money.repositories.SavingsGoalRepository
import com.juanitos.data.money.repositories.TransactionRepository

/**
 * Real [AppContainer] wired to a fresh in-memory Room database, for tests.
 * Same repository construction as [com.juanitos.data.AppDataContainer], just pointed at
 * [JuanitOSDatabase.buildInMemory] instead of the on-disk singleton.
 */
class TestAppContainer(context: Context) : AppContainer {
    val database: JuanitOSDatabase = JuanitOSDatabase.buildInMemory(context)

    override val accountRepository: AccountRepository by lazy {
        OfflineAccountRepository(accountDao = database.accountDao())
    }
    override val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(
            transactionDao = database.transactionDao(),
            accountDao = database.accountDao(),
            monthlySummaryDao = database.monthlySummaryDao(),
        )
    }
    override val categoryRepository: CategoryRepository by lazy {
        OfflineCategoryRepository(categoryDao = database.categoryDao())
    }
    override val creditCardRepository: CreditCardRepository by lazy {
        OfflineCreditCardRepository(
            creditCardDao = database.creditCardDao(),
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
            accountDao = database.accountDao(),
            monthlySummaryDao = database.monthlySummaryDao(),
        )
    }
    override val monthlySummaryRepository: MonthlySummaryRepository by lazy {
        OfflineMonthlySummaryRepository(
            monthlySummaryDao = database.monthlySummaryDao(),
            transactionDao = database.transactionDao(),
        )
    }
    override val savingsGoalRepository: SavingsGoalRepository by lazy {
        OfflineSavingsGoalRepository(savingsGoalDao = database.savingsGoalDao())
    }
}
