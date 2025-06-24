package com.juanitos.data.money.repositories

import com.juanitos.data.money.entities.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun insert(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    fun getById(id: Int): Flow<Transaction>
}
