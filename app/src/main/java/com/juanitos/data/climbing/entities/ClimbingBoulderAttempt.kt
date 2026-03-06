package com.juanitos.data.climbing.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "climbing_boulder_attempts",
    foreignKeys = [
        ForeignKey(
            entity = ClimbingWorkout::class,
            parentColumns = ["id"],
            childColumns = ["climbing_workout_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClimbingBoulder::class,
            parentColumns = ["id"],
            childColumns = ["climbing_boulder_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = ClimbingMedia::class,
            parentColumns = ["id"],
            childColumns = ["video_media_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("climbing_workout_id"), Index("climbing_boulder_id"), Index("video_media_id")]
)
data class ClimbingBoulderAttempt(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "climbing_workout_id")
    val climbingWorkoutId: Int,
    @ColumnInfo(name = "climbing_boulder_id")
    val climbingBoulderId: Int,
    @ColumnInfo(name = "video_media_id")
    val videoMediaId: Int? = null,
    @ColumnInfo(name = "attempt_order", defaultValue = "0")
    val attemptOrder: Int? = 0,
    val notes: String? = null,
    @ColumnInfo(name = "created_at", defaultValue = "(datetime('now', 'localtime'))")
    val createdAt: String? = null,
)
