package com.juanitos.data

import android.content.Context

interface AppContainer {
    val settingsRepository: SettingsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val settingsRepository: SettingsRepository by lazy {
        OfflineSettingsRepository(settingDao = JuanitOSDatabase.getDatabase(context).settingDao())
    }
}
