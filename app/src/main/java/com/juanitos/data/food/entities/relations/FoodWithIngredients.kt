package com.juanitos.data.food.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.juanitos.data.food.entities.BatchFood
import com.juanitos.data.food.entities.BatchFoodIngredient
import com.juanitos.data.food.entities.Food
import com.juanitos.data.food.entities.FoodIngredient
import com.juanitos.data.food.entities.Ingredient
import kotlin.math.roundToInt

// Fetch food with ingredients and batch foods using @Embedded and @Relation annotations
data class FoodDetails(
    @Embedded
    val food: Food,
    @Relation(
        entity = FoodIngredient::class,
        parentColumn = "id",
        entityColumn = "food_id"
    )
    val foodIngredients: List<FoodIngredientDetails>
) {
    fun toFormattedFoodDetails(): FormattedFoodDetails {
        val totalCalories = foodIngredients.sumOf {
            val ingredientCalories = it.ingredient?.caloriesPer100 ?: 0
            val batchFoodCalories =
                it.batchFood?.batchFoodIngredients?.sumOf { batchFoodIngredient ->
                    val ingredientGrams =
                        batchFoodIngredient.batchFoodIngredient.grams
                    val ingredientCaloriesPer100 =
                        batchFoodIngredient.ingredient.caloriesPer100
                    (ingredientGrams * ingredientCaloriesPer100) / 100
                } ?: 0
            val batchFoodTotalGrams = it.batchFood?.batchFood?.totalGrams ?: 0

            val grams = it.foodIngredient.grams

            val batchFoodRatio =
                if (batchFoodTotalGrams > 0) {
                    grams / batchFoodTotalGrams.toDouble()
                } else {
                    0.0
                }

            (ingredientCalories * grams / 100) + (batchFoodCalories * batchFoodRatio)
        }
        val totalProteins = foodIngredients.sumOf {
            val ingredientProteins = it.ingredient?.proteinsPer100 ?: 0.0
            val batchFoodProteins =
                it.batchFood?.batchFoodIngredients?.sumOf { batchFoodIngredient ->
                    val ingredientGrams =
                        batchFoodIngredient.batchFoodIngredient.grams.toDouble()
                    val ingredientProteinsPer100 =
                        batchFoodIngredient.ingredient.proteinsPer100
                    (ingredientGrams * ingredientProteinsPer100) / 100.0
                } ?: 0.0

            val grams = it.foodIngredient.grams.toDouble()
            val batchFoodTotalGrams = it.batchFood?.batchFood?.totalGrams ?: 0
            val batchFoodRatio =
                if (batchFoodTotalGrams > 0) {
                    grams / (it.batchFood?.batchFood?.totalGrams ?: 1).toDouble()
                } else {
                    0.0
                }
            (ingredientProteins * grams / 100.0) + (batchFoodProteins * batchFoodRatio)
        }
        return FormattedFoodDetails(
            id = food.id,
            name = food.name,
            totalCalories = totalCalories.roundToInt(),
            totalProteins = totalProteins,
            createdAt = food.createdAt
        )
    }
}

data class FoodIngredientDetails(
    @Embedded
    val foodIngredient: FoodIngredient,
    @Relation(
        parentColumn = "ingredient_id",
        entityColumn = "id"
    )
    val ingredient: Ingredient?,
    @Relation(
        entity = BatchFood::class,
        parentColumn = "batch_food_id",
        entityColumn = "id"
    )
    val batchFood: BatchFoodDetails?,
)

data class BatchFoodDetails(
    @Embedded
    val batchFood: BatchFood,
    @Relation(
        entity = BatchFoodIngredient::class,
        parentColumn = "id",
        entityColumn = "batch_food_id"
    )
    val batchFoodIngredients: List<BatchFoodIngredientDetails>
) {
    fun toBatchFoodWithIngredientDetails(): BatchFoodWithIngredientDetails {
        return BatchFoodWithIngredientDetails(
            id = batchFood.id,
            name = batchFood.name,
            totalGrams = batchFood.totalGrams,
            gramsUsed = batchFood.gramsUsed,
            ingredients = batchFoodIngredients.map { batchFoodIngredientDetails ->
                IngredientDetail(
                    ingredientId = batchFoodIngredientDetails.ingredient.id,
                    batchFoodIngredientId = batchFoodIngredientDetails.batchFoodIngredient.id,
                    name = batchFoodIngredientDetails.ingredient.name,
                    caloriesPer100 = batchFoodIngredientDetails.ingredient.caloriesPer100,
                    proteinsPer100 = batchFoodIngredientDetails.ingredient.proteinsPer100,
                    grams = batchFoodIngredientDetails.batchFoodIngredient.grams
                )
            }
        )
    }
}

data class BatchFoodIngredientDetails(
    @Embedded
    val batchFoodIngredient: BatchFoodIngredient,
    @Relation(
        parentColumn = "ingredient_id",
        entityColumn = "id"
    )
    val ingredient: Ingredient
)

data class FormattedFoodDetails(
    val id: Int,
    val name: String,
    val totalCalories: Int,
    val totalProteins: Double,
    val createdAt: String? = null,
) {
    fun toFood(): Food {
        return Food(
            id = id,
            name = name,
        )
    }
}