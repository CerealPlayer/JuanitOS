package com.juanitos.data.habit.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_entries",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habit_id")]
)
data class HabitEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "habit_id")
    val habitId: Int,
    val date: String,
    val completed: Boolean = false,
    @ColumnInfo(name = "created_at", defaultValue = "(datetime('now', 'localtime'))")
    val createdAt: String? = null,
)
