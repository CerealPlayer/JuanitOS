package com.juanitos.data

import android.content.Context
import com.juanitos.data.money.offline.OfflineAccountRepository
import com.juanitos.data.money.offline.OfflineCategoryRepository
import com.juanitos.data.money.offline.OfflineCreditCardRepository
import com.juanitos.data.money.offline.OfflineMonthlySummaryRepository
import com.juanitos.data.money.offline.OfflineTransactionRepository
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.CreditCardRepository
import com.juanitos.data.money.repositories.MonthlySummaryRepository
import com.juanitos.data.money.repositories.TransactionRepository

interface AppContainer {
    val accountRepository: AccountRepository
    val transactionRepository: TransactionRepository
    val categoryRepository: CategoryRepository
    val creditCardRepository: CreditCardRepository
    val monthlySummaryRepository: MonthlySummaryRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    private val db by lazy { JuanitOSDatabase.getDatabase(context) }

    override val accountRepository: AccountRepository by lazy {
        OfflineAccountRepository(accountDao = db.accountDao())
    }
    override val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(
            transactionDao = db.transactionDao(),
            accountDao = db.accountDao(),
            monthlySummaryDao = db.monthlySummaryDao(),
        )
    }
    override val categoryRepository: CategoryRepository by lazy {
        OfflineCategoryRepository(categoryDao = db.categoryDao())
    }
    override val creditCardRepository: CreditCardRepository by lazy {
        OfflineCreditCardRepository(
            creditCardDao = db.creditCardDao(),
            transactionDao = db.transactionDao(),
            categoryDao = db.categoryDao(),
            accountDao = db.accountDao(),
            monthlySummaryDao = db.monthlySummaryDao(),
        )
    }
    override val monthlySummaryRepository: MonthlySummaryRepository by lazy {
        OfflineMonthlySummaryRepository(
            monthlySummaryDao = db.monthlySummaryDao(),
            transactionDao = db.transactionDao(),
        )
    }
}
