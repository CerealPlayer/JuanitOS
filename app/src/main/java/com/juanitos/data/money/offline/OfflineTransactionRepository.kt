package com.juanitos.data.money.offline

import com.juanitos.data.money.daos.TransactionDao
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.repositories.TransactionRepository

class OfflineTransactionRepository(private val transactionDao: TransactionDao) :
    TransactionRepository {
    override suspend fun insert(transaction: Transaction) = transactionDao.insert(transaction)
    override suspend fun update(transaction: Transaction) = transactionDao.update(transaction)
    override suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)
    override fun getById(id: Int) = transactionDao.getById(id)
}