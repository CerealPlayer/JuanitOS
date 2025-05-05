package com.juanitos.data.food.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.juanitos.data.food.entities.BatchFood
import com.juanitos.data.food.entities.BatchFoodIngredient
import com.juanitos.data.food.entities.Ingredient

data class BatchFoodWithIngredients(
    @Embedded
    val batchFood: BatchFood,
    @Relation(
        entity = BatchFoodIngredient::class,
        parentColumn = "id",
        entityColumn = "batch_food_id"
    )
    val ingredientsWithDetails: List<BatchFoodIngredientWithDetails>
) {
    fun toBatchFoodWithIngredientDetails(): BatchFoodWithIngredientDetails {
        return BatchFoodWithIngredientDetails(
            id = batchFood.id,
            name = batchFood.name,
            totalGrams = batchFood.totalGrams,
            ingredients = ingredientsWithDetails.map { ingredientWithDetails ->
                IngredientDetail(
                    id = ingredientWithDetails.ingredient.id,
                    name = ingredientWithDetails.ingredient.name,
                    caloriesPer100 = ingredientWithDetails.ingredient.caloriesPer100,
                    grams = ingredientWithDetails.batchFoodIngredient.grams
                )
            }
        )
    }
}

data class BatchFoodIngredientWithDetails(
    @Embedded
    val batchFoodIngredient: BatchFoodIngredient,
    @Relation(
        parentColumn = "ingredient_id",
        entityColumn = "id"
    )
    val ingredient: Ingredient
)

data class BatchFoodWithIngredientDetails(
    val id: Int = 0,
    val name: String,
    val totalGrams: Int,
    val ingredients: List<IngredientDetail>
)

data class IngredientDetail(
    val id: Int = 0,
    val name: String,
    val caloriesPer100: String,
    val grams: String,
)
