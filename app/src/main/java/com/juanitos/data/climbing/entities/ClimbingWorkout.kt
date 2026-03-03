package com.juanitos.data.climbing.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "climbing_workouts")
data class ClimbingWorkout(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val name: String,
    val date: String,
    @ColumnInfo(name = "start_time")
    val startTime: String? = null,
    @ColumnInfo(name = "end_time")
    val endTime: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "created_at", defaultValue = "(datetime('now', 'localtime'))")
    val createdAt: String? = null,
)
