package com.juanitos.data.money.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_goals",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["account_id"], unique = true)]
)
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "account_id")
    val accountId: Int,
    // GoalType.name: "FIXED" | "PERCENTAGE"
    @ColumnInfo(name = "goal_type")
    val goalType: String,
    @ColumnInfo(name = "fixed_amount")
    val fixedAmount: Double? = null,
    // e.g. 5.0 for 5%
    val percentage: Double? = null,
    @ColumnInfo(name = "notifications_enabled", defaultValue = "0")
    val notificationsEnabled: Boolean = false,
    // "YYYY-MM" dedupe key so the daily risk check only notifies once per month
    @ColumnInfo(name = "last_risk_notified_month")
    val lastRiskNotifiedMonth: String? = null,
    @ColumnInfo(name = "created_at", defaultValue = "(datetime('now', 'localtime'))")
    val createdAt: String? = null,
)
