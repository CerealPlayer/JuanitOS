package com.juanitos.data.food.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.juanitos.data.food.entities.BatchFoodIngredient
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchFoodIngredientDao {
    @Insert
    suspend fun insert(batchFoodIngredient: BatchFoodIngredient): Long

    @Update
    suspend fun update(batchFoodIngredient: BatchFoodIngredient)

    @Upsert
    suspend fun upsert(batchFoodIngredient: BatchFoodIngredient)

    @Delete
    suspend fun delete(batchFoodIngredient: BatchFoodIngredient)

    @Query("select * from batch_food_ingredients where id = :id")
    fun getBatchFoodIngredient(id: Int): Flow<BatchFoodIngredient>

    @Query("select * from batch_food_ingredients order by id asc")
    fun getAllBatchFoodIngredients(): Flow<List<BatchFoodIngredient>>
}