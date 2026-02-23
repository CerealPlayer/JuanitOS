package com.juanitos.data.workout.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.workout.entities.WorkoutSet
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {
    @Insert
    suspend fun insert(workoutSet: WorkoutSet): Long

    @Update
    suspend fun update(workoutSet: WorkoutSet)

    @Delete
    suspend fun delete(workoutSet: WorkoutSet)

    @Query("SELECT * FROM workout_sets WHERE workout_exercise_id = :workoutExerciseId ORDER BY position ASC")
    fun getByWorkoutExerciseId(workoutExerciseId: Int): Flow<List<WorkoutSet>>
}
