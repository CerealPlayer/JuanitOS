package com.juanitos.data

import android.content.Context
import com.juanitos.data.money.offline.OfflineAccountRepository
import com.juanitos.data.money.offline.OfflineCategoryRepository
import com.juanitos.data.money.offline.OfflineCreditCardRepository
import com.juanitos.data.money.offline.OfflineTransactionRepository
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.CreditCardRepository
import com.juanitos.data.money.repositories.TransactionRepository

interface AppContainer {
    val accountRepository: AccountRepository
    val transactionRepository: TransactionRepository
    val categoryRepository: CategoryRepository
    val creditCardRepository: CreditCardRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val accountRepository: AccountRepository by lazy {
        OfflineAccountRepository(accountDao = JuanitOSDatabase.getDatabase(context).accountDao())
    }
    override val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(
            transactionDao = JuanitOSDatabase.getDatabase(context).transactionDao()
        )
    }
    override val categoryRepository: CategoryRepository by lazy {
        OfflineCategoryRepository(
            categoryDao = JuanitOSDatabase.getDatabase(context).categoryDao()
        )
    }
    override val creditCardRepository: CreditCardRepository by lazy {
        OfflineCreditCardRepository(
            creditCardDao = JuanitOSDatabase.getDatabase(context).creditCardDao(),
            transactionDao = JuanitOSDatabase.getDatabase(context).transactionDao(),
            categoryDao = JuanitOSDatabase.getDatabase(context).categoryDao()
        )
    }
}
