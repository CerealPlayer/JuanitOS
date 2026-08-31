package com.juanitos.data.money.repositories

import com.juanitos.data.money.entities.CreditCard
import kotlinx.coroutines.flow.Flow

interface CreditCardRepository {
    suspend fun insert(accountId: Int, name: String, paymentDay: Int): Long
    suspend fun update(creditCard: CreditCard)
    suspend fun delete(creditCard: CreditCard)
    fun getById(id: Int): Flow<CreditCard>
    fun getAll(): Flow<List<CreditCard>>
    fun getByAccountId(accountId: Int): Flow<List<CreditCard>>
    suspend fun generateDueSettlements(accountId: Int)
}
