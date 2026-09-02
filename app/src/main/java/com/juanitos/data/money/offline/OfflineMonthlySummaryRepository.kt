package com.juanitos.data.money.offline

import com.juanitos.data.money.daos.MonthlySummaryDao
import com.juanitos.data.money.daos.TransactionDao
import com.juanitos.data.money.entities.MonthlySummary
import com.juanitos.data.money.repositories.MonthlySummaryRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

class OfflineMonthlySummaryRepository(
    private val monthlySummaryDao: MonthlySummaryDao,
    private val transactionDao: TransactionDao,
) : MonthlySummaryRepository {
    override fun getByAccount(accountId: Int): Flow<List<MonthlySummary>> =
        monthlySummaryDao.getByAccount(accountId)

    override suspend fun getByAccountAndMonth(accountId: Int, month: String): MonthlySummary? =
        monthlySummaryDao.getByAccountAndMonth(accountId, month)

    // Self-healing recompute of the single most-recently-elapsed month, guarding against drift
    // (e.g. a crash between a transaction write and its incremental summary upsert). The current
    // month's row is kept live by the incremental adjust() calls at transaction insert/delete
    // sites and is never touched here.
    override suspend fun finalizeElapsedMonths(accountId: Int) {
        val previousMonth = YearMonth.now().minusMonths(1).toString()
        val totals = transactionDao.sumIncomeAndExpenses(accountId, previousMonth)
        monthlySummaryDao.upsert(
            MonthlySummary(
                accountId = accountId,
                month = previousMonth,
                totalIncome = totals.income,
                totalExpenses = totals.expenses,
                netBalance = totals.income - totals.expenses,
            )
        )
    }
}
