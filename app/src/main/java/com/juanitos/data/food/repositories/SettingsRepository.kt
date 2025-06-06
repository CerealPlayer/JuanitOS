package com.juanitos.data.food.repositories

import com.juanitos.data.food.entities.Setting
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAllSettingsStream(): Flow<List<Setting>>
    fun getSettingStream(id: Int): Flow<Setting?>
    fun getByName(name: String): Flow<Setting?>
    suspend fun insertSetting(name: String, value: String)
    suspend fun deleteSetting(setting: Setting)
    suspend fun updateSetting(setting: Setting)
}