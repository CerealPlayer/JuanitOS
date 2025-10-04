package com.juanitos.data.food.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.food.entities.HealthStat
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthStatDao {
    @Query("insert into health_stats (weight, activity_factor) values (:weight, :factor)")
    suspend fun insert(weight: Float, factor: Int)

    @Update
    suspend fun update(stat: HealthStat)

    @Delete
    suspend fun delete(stat: HealthStat)

    @Query("select * from health_stats order by created_at desc limit 1")
    fun getLatestStat(): Flow<HealthStat?>
}