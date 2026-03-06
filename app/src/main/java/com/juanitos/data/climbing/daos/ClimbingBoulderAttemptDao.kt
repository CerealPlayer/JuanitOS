package com.juanitos.data.climbing.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.climbing.entities.ClimbingBoulderAttempt
import kotlinx.coroutines.flow.Flow

@Dao
interface ClimbingBoulderAttemptDao {
    @Insert
    suspend fun insert(climbingBoulderAttempt: ClimbingBoulderAttempt): Long

    @Update
    suspend fun update(climbingBoulderAttempt: ClimbingBoulderAttempt)

    @Delete
    suspend fun delete(climbingBoulderAttempt: ClimbingBoulderAttempt)

    @Query("SELECT * FROM climbing_boulder_attempts ORDER BY climbing_workout_id DESC, boulder_order ASC, attempt_order ASC, id ASC")
    fun getAll(): Flow<List<ClimbingBoulderAttempt>>

    @Query(
        """
        SELECT * FROM climbing_boulder_attempts
        WHERE climbing_workout_id = :climbingWorkoutId
        ORDER BY boulder_order ASC, attempt_order ASC, id ASC
        """
    )
    fun getByClimbingWorkoutId(climbingWorkoutId: Int): Flow<List<ClimbingBoulderAttempt>>
}
