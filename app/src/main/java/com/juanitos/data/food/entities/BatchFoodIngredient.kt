package com.juanitos.data.food.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "batch_food_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = BatchFood::class,
            parentColumns = ["id"],
            childColumns = ["batch_food_id"],
            onDelete = ForeignKey.CASCADE
        ),
    ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredient_id"],
            onDelete = ForeignKey.CASCADE
    )
    ]
)
data class BatchFoodIngredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "batch_food_id")
    val batchFoodId: Int,
    @ColumnInfo(name = "ingredient_id")
    val ingredientId: Int,
    val grams: String,
)
