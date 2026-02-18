package com.juanitos.data.money.offline

import com.juanitos.data.money.daos.FixedSpendingDao
import com.juanitos.data.money.entities.FixedSpending
import com.juanitos.data.money.repositories.FixedSpendingRepository

class OfflineFixedSpendingRepository(private val fixedSpendingDao: FixedSpendingDao) :
    FixedSpendingRepository {
    override suspend fun insert(fixedSpending: FixedSpending) =
        fixedSpendingDao.insert(
            fixedSpending.amount,
            fixedSpending.categoryId,
            fixedSpending.description
        )

    override suspend fun update(fixedSpending: FixedSpending) =
        fixedSpendingDao.update(fixedSpending)

    override suspend fun delete(fixedSpending: FixedSpending) =
        fixedSpendingDao.delete(fixedSpending)

    override fun getById(id: Int) = fixedSpendingDao.getById(id)
}