package com.juanitos.ui.routes.money

import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import com.juanitos.data.money.entities.relations.TransactionWithCategory
import com.juanitos.lib.parseDbDatetimeToLocalDate
import java.time.LocalDate

sealed interface Movement {
    val date: LocalDate?
    val sortId: Int

    data class FixedSpendingMovement(
        val fixedSpending: FixedSpendingWithCategory,
        override val date: LocalDate?,
    ) : Movement {
        override val sortId: Int get() = fixedSpending.fixedSpending.id
    }

    data class TransactionMovement(
        val transaction: TransactionWithCategory,
    ) : Movement {
        override val date: LocalDate? get() = parseDbDatetimeToLocalDate(transaction.transaction.createdAt)
        override val sortId: Int get() = transaction.transaction.id
    }
}
