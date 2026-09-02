package com.juanitos.data.money.repositories

import com.juanitos.data.money.entities.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun insert(transaction: Transaction): Long

    // No UI edits a transaction's amount/date today. If that changes, this must also apply a
    // current_balance/monthly-summary delta (old amount reversed, new amount applied), mirroring
    // insert()/delete() in OfflineTransactionRepository.
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    fun getById(id: Int): Flow<Transaction>
    suspend fun generateDueOccurrences(accountId: Int)
    suspend fun applyDuePendingTransactions(accountId: Int)
}
