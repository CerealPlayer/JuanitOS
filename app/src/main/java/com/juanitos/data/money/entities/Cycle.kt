package com.juanitos.data.money.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycles")
class Cycle(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "start_date", defaultValue = "(datetime('now', 'localtime'))")
    val startDate: String? = null,
    val endDate: String? = null,
    val totalIncome: Double = 0.0,
)