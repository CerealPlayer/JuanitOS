package com.juanitos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(setting: Setting)

    @Update
    suspend fun update(setting: Setting)

    @Delete
    suspend fun delete(setting: Setting)

    @Query("select * from settings where id = :id")
    fun getSetting(id: Int): Flow<Setting>

    @Query("select * from settings order by created_at asc")
    fun getAllSettings(): Flow<List<Setting>>

    @Query("select * from settings where setting_name = :name limit 1")
    fun getByName(name: String): Flow<Setting?>
}