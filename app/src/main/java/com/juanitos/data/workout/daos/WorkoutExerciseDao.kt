package com.juanitos.data.workout.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.juanitos.data.workout.entities.WorkoutExercise
import com.juanitos.data.workout.entities.relations.WorkoutExerciseWithSets
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutExerciseDao {
    @Insert
    suspend fun insert(workoutExercise: WorkoutExercise): Long

    @Update
    suspend fun update(workoutExercise: WorkoutExercise)

    @Delete
    suspend fun delete(workoutExercise: WorkoutExercise)

    @Transaction
    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    fun getByIdWithSets(id: Int): Flow<WorkoutExerciseWithSets>

    @Transaction
    @Query("SELECT * FROM workout_exercises WHERE workout_id = :workoutId ORDER BY position ASC")
    fun getByWorkoutIdWithSets(workoutId: Int): Flow<List<WorkoutExerciseWithSets>>
}
