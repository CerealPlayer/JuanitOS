package com.juanitos.data.climbing.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.climbing.entities.ClimbingMedia
import kotlinx.coroutines.flow.Flow

@Dao
interface ClimbingMediaDao {
    @Insert
    suspend fun insert(climbingMedia: ClimbingMedia): Long

    @Update
    suspend fun update(climbingMedia: ClimbingMedia)

    @Delete
    suspend fun delete(climbingMedia: ClimbingMedia)

    @Query("SELECT * FROM climbing_media ORDER BY id DESC")
    fun getAll(): Flow<List<ClimbingMedia>>
}
