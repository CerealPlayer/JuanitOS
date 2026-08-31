package com.juanitos.data.money.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.juanitos.data.money.entities.Account
import com.juanitos.data.money.entities.Transaction

data class AccountWithDetails(
    @Embedded
    val account: Account,
    @Relation(
        entity = Transaction::class,
        parentColumn = "id",
        entityColumn = "account_id"
    )
    val transactions: List<TransactionWithCategory>,
)
