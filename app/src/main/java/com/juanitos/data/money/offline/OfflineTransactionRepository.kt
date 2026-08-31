package com.juanitos.data.money.offline

import com.juanitos.data.money.daos.TransactionDao
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.money.repositories.TransactionRepository
import com.juanitos.lib.TransactionFrequency
import com.juanitos.lib.clampDayOfMonth
import com.juanitos.lib.formatLocalDateToDbDatetime
import com.juanitos.lib.parseDbDatetimeToLocalDate
import java.time.LocalDate
import java.time.YearMonth

class OfflineTransactionRepository(private val transactionDao: TransactionDao) :
    TransactionRepository {
    override suspend fun insert(transaction: Transaction) =
        transactionDao.insert(
            transaction.accountId,
            transaction.amount,
            transaction.categoryId,
            transaction.description,
            transaction.createdAt ?: formatLocalDateToDbDatetime(LocalDate.now()),
            transaction.frequency,
            transaction.recurrenceRootId,
            transaction.creditCardId
        )

    override suspend fun update(transaction: Transaction) = transactionDao.update(transaction)

    override suspend fun delete(transaction: Transaction) {
        if (transaction.frequency != null) {
            transactionDao.deleteByRecurrenceRoot(transaction.id)
        }
        transactionDao.delete(transaction)
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
                transactionDao.insert(
                    accountId = template.accountId,
                    amount = template.amount,
                    category = template.categoryId,
                    description = template.description,
                    createdAt = formatLocalDateToDbDatetime(nextDate),
                    frequency = null,
                    recurrenceRootId = template.id,
                    creditCardId = template.creditCardId
                )
                latestDate = nextDate
            }
        }
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
