package com.juanitos

import android.app.Application
import com.juanitos.data.AppContainer
import com.juanitos.data.AppDataContainer

class JuanitOSApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
