package com.juanitos.data.money.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val name: String,
    @ColumnInfo(name = "starting_balance")
    val startingBalance: Double = 0.0,
    @ColumnInfo(name = "is_selected", defaultValue = "0")
    val isSelected: Boolean = false,
    @ColumnInfo(name = "created_at", defaultValue = "(datetime('now', 'localtime'))")
    val createdAt: String? = null,
)
