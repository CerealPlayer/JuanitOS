package com.juanitos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Insert
    suspend fun insert(ingredient: Ingredient)

    @Update
    suspend fun update(ingredient: Ingredient)

    @Delete
    suspend fun delete(ingredient: Ingredient)

    @Query("select * from ingredients where id = :id")
    fun getIngredient(id: Int): Flow<Ingredient>

    @Query("select * from ingredients order by id asc")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("select * from ingredients where name like '%' || :query || '%' order by id asc")
    fun searchIngredients(query: String): Flow<List<Ingredient>>
}
