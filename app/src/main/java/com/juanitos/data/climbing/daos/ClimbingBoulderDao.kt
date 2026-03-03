package com.juanitos.data.climbing.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.climbing.entities.ClimbingBoulder
import kotlinx.coroutines.flow.Flow

@Dao
interface ClimbingBoulderDao {
    @Insert
    suspend fun insert(climbingBoulder: ClimbingBoulder): Long

    @Update
    suspend fun update(climbingBoulder: ClimbingBoulder)

    @Delete
    suspend fun delete(climbingBoulder: ClimbingBoulder)

    @Query("SELECT * FROM climbing_boulders ORDER BY id DESC")
    fun getAll(): Flow<List<ClimbingBoulder>>
}
