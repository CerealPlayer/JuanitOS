package com.juanitos.data.climbing.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "climbing_boulders",
    foreignKeys = [
        ForeignKey(
            entity = ClimbingMedia::class,
            parentColumns = ["id"],
            childColumns = ["picture_media_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("picture_media_id")]
)
data class ClimbingBoulder(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val grade: String,
    val style: String? = null,
    @ColumnInfo(name = "picture_media_id")
    val pictureMediaId: Int? = null,
    @ColumnInfo(name = "created_at", defaultValue = "(datetime('now', 'localtime'))")
    val createdAt: String? = null,
)
