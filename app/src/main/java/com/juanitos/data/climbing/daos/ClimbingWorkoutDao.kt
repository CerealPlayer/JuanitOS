package com.juanitos.data.climbing.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.climbing.entities.ClimbingWorkout
import kotlinx.coroutines.flow.Flow

@Dao
interface ClimbingWorkoutDao {
    @Insert
    suspend fun insert(climbingWorkout: ClimbingWorkout): Long

    @Update
    suspend fun update(climbingWorkout: ClimbingWorkout)

    @Delete
    suspend fun delete(climbingWorkout: ClimbingWorkout)

    @Query("SELECT * FROM climbing_workouts ORDER BY date DESC")
    fun getAll(): Flow<List<ClimbingWorkout>>
}
