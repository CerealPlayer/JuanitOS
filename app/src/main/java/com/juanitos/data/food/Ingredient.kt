package com.juanitos.data.food

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name ="calories_per_100")
    val caloriesPer100: String,
    @ColumnInfo(name ="proteins_per_100")
    val proteinsPer100: String,
)
