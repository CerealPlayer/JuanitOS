package com.juanitos.data.food.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.juanitos.data.food.entities.BatchFood
import com.juanitos.data.food.entities.relations.BatchFoodWithIngredients
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchFoodDao {
    @Query("insert into batch_foods (name, total_grams) values (:name, :totalGrams)")
    suspend fun insert(name: String, totalGrams: Int): Long

    @Update
    suspend fun update(batchFood: BatchFood)

    @Delete
    suspend fun delete(batchFood: BatchFood)

    @Query("select * from batch_foods where id = :id")
    fun getBatchFood(id: Int): Flow<BatchFood>

    @Query("select * from batch_foods order by id asc")
    fun getAllBatchFoods(): Flow<List<BatchFood>>

    @Transaction
    @Query("select * from batch_foods")
    fun getBatchFoodsWithIngredients(): Flow<List<BatchFoodWithIngredients>>

    @Transaction
    @Query("select * from batch_foods where name like '%' || :query || '%' order by id asc")
    fun searchBatchFoods(query: String): Flow<List<BatchFoodWithIngredients>>
}
