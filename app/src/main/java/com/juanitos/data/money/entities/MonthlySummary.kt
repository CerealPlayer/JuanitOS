package com.juanitos.data.money.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_summaries",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["account_id", "month"], unique = true)]
)
data class MonthlySummary(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "account_id")
    val accountId: Int,
    // "YYYY-MM", matching the prefix of Transaction.createdAt
    val month: String,
    @ColumnInfo(name = "total_income")
    val totalIncome: Double,
    @ColumnInfo(name = "total_expenses")
    val totalExpenses: Double,
    @ColumnInfo(name = "net_balance")
    val netBalance: Double,
)
