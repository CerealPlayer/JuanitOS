package com.juanitos.data.food.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name ="calories_per_100")
    val caloriesPer100: Int,
    @ColumnInfo(name ="proteins_per_100")
    val proteinsPer100: Double,
)
