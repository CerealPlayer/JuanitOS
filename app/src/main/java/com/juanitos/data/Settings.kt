package com.juanitos.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name="setting_name")
    val settingName: String,
    @ColumnInfo(name="setting_value")
    val settingValue: String,
    @ColumnInfo(name="created_at",defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String? = null
)
