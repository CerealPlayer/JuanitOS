package com.juanitos.data

import kotlinx.coroutines.flow.Flow

class OfflineSettingsRepository(private val settingDao: SettingDao) : SettingsRepository {
    override fun getAllSettingsStream(): Flow<List<Setting>> = settingDao.getAllSettings()
    override fun getSettingStream(id: Int): Flow<Setting?> = settingDao.getSetting(id)
    override fun getByName(name: String): Flow<Setting?> = settingDao.getByName(name)
    override suspend fun insertSetting(setting: Setting) = settingDao.insert(setting)
    override suspend fun deleteSetting(setting: Setting) = settingDao.delete(setting)
    override suspend fun updateSetting(setting: Setting) = settingDao.update(setting)
}