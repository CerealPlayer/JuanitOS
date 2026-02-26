package com.juanitos.data.habit.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val name: String,
    val description: String? = null,
    @ColumnInfo(name = "created_at", defaultValue = "(datetime('now', 'localtime'))")
    val createdAt: String? = null,
)
