package com.juanitos.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val setting_name: String,
    val setting_value: String,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    val created_at: String = "CURRENT_TIMESTAMP"
)
