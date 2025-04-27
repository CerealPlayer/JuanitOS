package com.juanitos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Query("insert into settings (setting_name, setting_value) values (:name, :value)")
    suspend fun insert(name: String, value: String)

    @Update
    suspend fun update(setting: Setting)

    @Delete
    suspend fun delete(setting: Setting)

    @Query("select * from settings where id = :id")
    fun getSetting(id: Int): Flow<Setting>

    @Query("select * from settings order by created_at asc")
    fun getAllSettings(): Flow<List<Setting>>

    @Query("select * from settings where setting_name = :name order by created_at desc limit 1")
    fun getByName(name: String): Flow<Setting?>
}