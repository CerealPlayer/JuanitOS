package com.juanitos.data.money.offline

import com.juanitos.data.money.CREDIT_CARD_PAYMENT_CATEGORY_NAME
import com.juanitos.data.money.daos.CategoryDao
import com.juanitos.data.money.daos.CreditCardDao
import com.juanitos.data.money.daos.TransactionDao
import com.juanitos.data.money.entities.CreditCard
import com.juanitos.data.money.repositories.CreditCardRepository
import com.juanitos.lib.clampDayOfMonth
import com.juanitos.lib.formatLocalDateToDbDatetime
import com.juanitos.lib.parseDbDatetimeToLocalDate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

class OfflineCreditCardRepository(
    private val creditCardDao: CreditCardDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
) : CreditCardRepository {
    override suspend fun insert(accountId: Int, name: String, paymentDay: Int): Long =
        creditCardDao.insert(accountId, name, paymentDay)

    override suspend fun update(creditCard: CreditCard) = creditCardDao.update(creditCard)
    override suspend fun delete(creditCard: CreditCard) = creditCardDao.delete(creditCard)
    override fun getById(id: Int): Flow<CreditCard> = creditCardDao.getById(id)
    override fun getAll(): Flow<List<CreditCard>> = creditCardDao.getAll()
    override fun getByAccountId(accountId: Int): Flow<List<CreditCard>> =
        creditCardDao.getByAccountId(accountId)

    override suspend fun generateDueSettlements(accountId: Int) {
        val today = LocalDate.now()
        creditCardDao.getByAccountIdOnce(accountId).forEach { card ->
            var cursor =
                parseDbDatetimeToLocalDate(card.lastSettledAt ?: card.createdAt) ?: return@forEach

            while (true) {
                val nextMonth = YearMonth.from(cursor).plusMonths(1)
                val nextPaymentDate = clampDayOfMonth(nextMonth, card.paymentDay)
                if (nextPaymentDate.isAfter(today)) break

                val after = formatLocalDateToDbDatetime(cursor)
                val upTo = formatLocalDateToDbDatetime(nextPaymentDate)
                val sum = transactionDao.sumCreditCardTransactions(card.id, after, upTo)
                if (sum != 0.0) {
                    val categoryId =
                        categoryDao.getByName(CREDIT_CARD_PAYMENT_CATEGORY_NAME)?.id
                    if (categoryId != null) {
                        transactionDao.insert(
                            accountId = card.accountId,
                            amount = sum,
                            category = categoryId,
                            description = "Card payment: ${card.name}",
                            createdAt = upTo,
                            frequency = null,
                            recurrenceRootId = null,
                            creditCardId = null
                        )
                    }
                }
                creditCardDao.updateLastSettledAt(card.id, upTo)
                cursor = nextPaymentDate
            }
        }
    }
}
