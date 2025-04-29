package com.juanitos.data.food

import kotlinx.coroutines.flow.Flow

class OfflineIngredientRepository(private val ingredientDao: IngredientDao) : IngredientRepository {
    override fun getAllIngredientsStream(): Flow<List<Ingredient>> = ingredientDao.getAllIngredients()
    override fun getIngredientStream(id: Int): Flow<Ingredient?> = ingredientDao.getIngredient(id)
    override fun searchIngredientsStream(query: String): Flow<List<Ingredient>> = ingredientDao.searchIngredients(query)
    override suspend fun insertIngredient(ingredient: Ingredient) = ingredientDao.insert(ingredient)
    override suspend fun deleteIngredient(ingredient: Ingredient) = ingredientDao.delete(ingredient)
    override suspend fun updateIngredient(ingredient: Ingredient) = ingredientDao.update(ingredient)
}
