package com.juanitos.data.food.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_stats")
data class HealthStat(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weight: Float,
    @ColumnInfo(name = "activity_factor")
    val activityFactor: Int,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String? = null
)
