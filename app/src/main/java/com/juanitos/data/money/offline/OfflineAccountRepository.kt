package com.juanitos.data.money.offline

import com.juanitos.data.money.daos.AccountDao
import com.juanitos.data.money.entities.Account
import com.juanitos.data.money.entities.relations.AccountWithDetails
import com.juanitos.data.money.repositories.AccountRepository
import kotlinx.coroutines.flow.Flow

class OfflineAccountRepository(private val accountDao: AccountDao) : AccountRepository {
    override suspend fun insert(name: String, startingBalance: Double): Long =
        accountDao.insert(name, startingBalance)

    override suspend fun update(account: Account) = accountDao.update(account)
    override suspend fun delete(account: Account) = accountDao.delete(account)
    override fun getAll(): Flow<List<Account>> = accountDao.getAll()
    override fun getSelected(): Flow<AccountWithDetails?> = accountDao.getSelected()
    override suspend fun selectAccount(id: Int) = accountDao.selectAccount(id)
}
