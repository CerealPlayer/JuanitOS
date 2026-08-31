package com.juanitos.data.money.repositories

import com.juanitos.data.money.entities.Account
import com.juanitos.data.money.entities.relations.AccountWithDetails
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    suspend fun insert(name: String, startingBalance: Double): Long
    suspend fun update(account: Account)
    suspend fun delete(account: Account)
    fun getAll(): Flow<List<Account>>
    fun getSelected(): Flow<AccountWithDetails?>
    suspend fun selectAccount(id: Int)
}
