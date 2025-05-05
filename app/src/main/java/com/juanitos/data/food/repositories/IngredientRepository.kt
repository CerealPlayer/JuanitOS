package com.juanitos.data.food.repositories

import com.juanitos.data.food.entities.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun getAllIngredientsStream(): Flow<List<Ingredient>>
    fun getIngredientStream(id: Int): Flow<Ingredient?>
    fun searchIngredientsStream(query: String): Flow<List<Ingredient>>
    suspend fun insertIngredient(ingredient: Ingredient)
    suspend fun deleteIngredient(ingredient: Ingredient)
    suspend fun updateIngredient(ingredient: Ingredient)
}
