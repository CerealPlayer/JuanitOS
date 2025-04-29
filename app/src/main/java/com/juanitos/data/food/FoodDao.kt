package com.juanitos.data.food

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("insert into foods (name) values (:name)")
    suspend fun insert(name: String): Long

    @Update
    suspend fun update(food: Food)

    @Delete
    suspend fun delete(food: Food)

    @Query("select * from foods where id = :id")
    fun getFood(id: Int): Flow<Food>

    @Query("select * from foods order by id asc")
    fun getAllFoods(): Flow<List<Food>>

    @Query("select * from foods where date(created_at) = date('now')")
    fun getTodaysFoods(): Flow<List<Food>>
}
