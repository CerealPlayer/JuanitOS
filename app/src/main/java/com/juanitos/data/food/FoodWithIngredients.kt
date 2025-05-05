package com.juanitos.data.food

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.juanitos.data.food.entities.Food
import com.juanitos.data.food.entities.Ingredient

data class FoodIngredientDetails(
    @Embedded(prefix = "food_") val food: Food,
    @Embedded(prefix = "ingredient_") val ingredient: Ingredient,
    @ColumnInfo(name ="grams") val grams: String,
)

data class IngredientWithGrams(
    val ingredient: Ingredient,
    val grams: String,
)

data class FoodWithIngredients(
    val food: Food,
    val ingredients: List<IngredientWithGrams>,
)
