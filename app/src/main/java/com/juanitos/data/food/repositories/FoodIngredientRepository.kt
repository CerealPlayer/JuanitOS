package com.juanitos.data.food.repositories

import com.juanitos.data.food.entities.FoodIngredient
import kotlinx.coroutines.flow.Flow

interface FoodIngredientRepository {
    fun getAllFoodIngredientsStream(): Flow<List<FoodIngredient>>
    fun getFoodIngredientStream(id: Int): Flow<FoodIngredient?>
    suspend fun insertFoodIngredient(foodIngredient: FoodIngredient)
    suspend fun deleteFoodIngredient(foodIngredient: FoodIngredient)
    suspend fun updateFoodIngredient(foodIngredient: FoodIngredient)
}
