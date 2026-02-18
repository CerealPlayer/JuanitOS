package com.juanitos.data

import android.content.Context
import com.juanitos.data.money.offline.OfflineCycleRepository
import com.juanitos.data.money.offline.OfflineFixedSpendingRepository
import com.juanitos.data.money.offline.OfflineTransactionRepository
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import com.juanitos.data.money.repositories.TransactionRepository

interface AppContainer {
    val cycleRepository: CycleRepository
    val transactionRepository: TransactionRepository
    val fixedSpendingRepository: FixedSpendingRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val cycleRepository: CycleRepository by lazy {
        OfflineCycleRepository(cycleDao = JuanitOSDatabase.getDatabase(context).cycleDao())
    }
    override val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(
            transactionDao = JuanitOSDatabase.getDatabase(context).transactionDao()
        )
    }
    override val fixedSpendingRepository: FixedSpendingRepository by lazy {
        OfflineFixedSpendingRepository(
            fixedSpendingDao = JuanitOSDatabase.getDatabase(context).fixedSpendingDao()
        )
    }
}
