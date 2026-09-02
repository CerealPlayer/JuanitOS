package com.juanitos.data.money.offline

import com.juanitos.data.money.daos.AccountDao
import com.juanitos.data.money.daos.MonthlySummaryDao
import com.juanitos.data.money.daos.TransactionDao
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.repositories.TransactionRepository
import com.juanitos.lib.TransactionFrequency
import com.juanitos.lib.clampDayOfMonth
import com.juanitos.lib.formatLocalDateToDbDatetime
import com.juanitos.lib.isBalanceAffecting
import com.juanitos.lib.parseDbDatetimeToLocalDate
import java.time.LocalDate
import java.time.YearMonth

class OfflineTransactionRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val monthlySummaryDao: MonthlySummaryDao,
) : TransactionRepository {
    override suspend fun insert(transaction: Transaction): Long {
        val createdAt = transaction.createdAt ?: formatLocalDateToDbDatetime(LocalDate.now())
        val id = transactionDao.insert(
            transaction.accountId,
            transaction.amount,
            transaction.categoryId,
            transaction.description,
            createdAt,
            transaction.frequency,
            transaction.recurrenceRootId,
            transaction.creditCardId
        )
        applyBalanceEffect(transaction.copy(createdAt = createdAt), transaction.amount)
        return id
    }

    override suspend fun update(transaction: Transaction) = transactionDao.update(transaction)

    override suspend fun delete(transaction: Transaction) {
        if (transaction.frequency != null) {
            val now = formatLocalDateToDbDatetime(LocalDate.now())
            val monthlyTotals = transactionDao.sumBalanceAffectingByRecurrenceRootGroupedByMonth(
                transaction.id,
                now
            )
            transactionDao.deleteByRecurrenceRoot(transaction.id)
            var totalReversal = 0.0
            monthlyTotals.forEach { (month, income, expenses) ->
                totalReversal += expenses - income
                monthlySummaryDao.adjust(transaction.accountId, month, -income, -expenses)
            }
            if (totalReversal != 0.0) accountDao.adjustBalance(transaction.accountId, totalReversal)
        }
        transactionDao.delete(transaction)
        applyBalanceEffect(transaction, -transaction.amount)
    }

    override fun getById(id: Int) = transactionDao.getById(id)

    override suspend fun generateDueOccurrences(accountId: Int) {
        val today = LocalDate.now()
        transactionDao.getRecurringTemplates(accountId).forEach { template ->
            val frequency =
                template.frequency?.let { TransactionFrequency.valueOf(it) } ?: return@forEach
            val anchorDayOfMonth = parseDbDatetimeToLocalDate(template.createdAt)?.dayOfMonth
            var latestDate =
                parseDbDatetimeToLocalDate(transactionDao.getLatestOccurrenceDate(template.id))
                    ?: return@forEach

            while (true) {
                val nextDate = nextOccurrence(latestDate, frequency, anchorDayOfMonth)
                if (nextDate.isAfter(today)) break
                val createdAt = formatLocalDateToDbDatetime(nextDate)
                transactionDao.insert(
                    accountId = template.accountId,
                    amount = template.amount,
                    category = template.categoryId,
                    description = template.description,
                    createdAt = createdAt,
                    frequency = null,
                    recurrenceRootId = template.id,
                    creditCardId = template.creditCardId
                )
                applyBalanceEffect(template.copy(createdAt = createdAt), template.amount)
                latestDate = nextDate
            }
        }
    }

    override suspend fun applyDuePendingTransactions(accountId: Int) {
        val now = formatLocalDateToDbDatetime(LocalDate.now())
        val lastSweptAt = accountDao.getLastSweptAt(accountId) ?: now
        val monthlyTotals =
            transactionDao.sumNewlyDueTransactionsGroupedByMonth(accountId, now, lastSweptAt)
        var totalDelta = 0.0
        monthlyTotals.forEach { (month, income, expenses) ->
            totalDelta += income - expenses
            monthlySummaryDao.adjust(accountId, month, income, expenses)
        }
        if (totalDelta != 0.0) accountDao.adjustBalance(accountId, totalDelta)
        accountDao.updateLastSweptAt(accountId, now)
    }

    /**
     * Applies [amount]'s balance/monthly-summary effect for [transaction] if it's balance-
     * affecting (not pending, not credit-card-linked). [amount] carries its own sign: pass the
     * transaction's own signed amount for an insert, or its negation for a reversal.
     */
    private suspend fun applyBalanceEffect(transaction: Transaction, amount: Double) {
        if (!isBalanceAffecting(transaction)) return
        accountDao.adjustBalance(transaction.accountId, -amount)
        val month = transaction.createdAt?.take(7) ?: return
        val incomeDelta = if (amount < 0) -amount else 0.0
        val expenseDelta = if (amount > 0) amount else 0.0
        monthlySummaryDao.adjust(transaction.accountId, month, incomeDelta, expenseDelta)
    }

    private fun nextOccurrence(
        from: LocalDate,
        frequency: TransactionFrequency,
        anchorDayOfMonth: Int?
    ): LocalDate = when (frequency) {
        TransactionFrequency.WEEKLY -> from.plusWeeks(1)
        TransactionFrequency.BIWEEKLY -> from.plusDays(15)
        TransactionFrequency.MONTHLY -> {
            val nextMonth = YearMonth.from(from).plusMonths(1)
            clampDayOfMonth(nextMonth, anchorDayOfMonth ?: from.dayOfMonth)
        }
    }
}
