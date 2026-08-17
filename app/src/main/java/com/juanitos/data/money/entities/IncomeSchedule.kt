package com.juanitos.data.money.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income_schedules")
data class IncomeSchedule(
    @PrimaryKey
    val id: Int = 1,
    @ColumnInfo(name = "day_of_month")
    val dayOfMonth: Int,
    val amount: Double,
)
