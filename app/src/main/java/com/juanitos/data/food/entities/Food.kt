package com.juanitos.data.food.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class Food(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "created_at", defaultValue = "(datetime('now', 'localtime'))")
    val createdAt: String? = null
)
