package com.juanitos.data.food.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "batch_foods")
data class BatchFood(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name : String,
    @ColumnInfo(name = "total_grams")
    val totalGrams: Int,
    @ColumnInfo(name = "grams_used")
    val gramsUsed: Int? = null,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String? = null,
)
