package com.juanitos.data.money.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_cards", foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CreditCard(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "account_id")
    val accountId: Int,
    val name: String,
    @ColumnInfo(name = "payment_day")
    val paymentDay: Int,
    @ColumnInfo(name = "last_settled_at")
    val lastSettledAt: String? = null,
    @ColumnInfo(name = "created_at", defaultValue = "(datetime('now', 'localtime'))")
    val createdAt: String? = null,
)
