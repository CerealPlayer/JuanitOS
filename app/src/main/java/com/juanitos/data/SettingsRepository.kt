package com.juanitos.data

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAllSettingsStream(): Flow<List<Setting>>
    fun getSettingStream(id: Int): Flow<Setting?>
    fun getByName(name: String): Flow<Setting?>
    suspend fun insertSetting(setting: Setting)
    suspend fun deleteSetting(setting: Setting)
    suspend fun updateSetting(setting: Setting)
}