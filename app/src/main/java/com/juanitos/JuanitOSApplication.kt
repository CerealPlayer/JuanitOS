package com.juanitos

import android.app.Application
import com.juanitos.data.AppContainer
import com.juanitos.data.AppDataContainer
import com.juanitos.data.money.work.CycleReconciliationWorker

class JuanitOSApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        CycleReconciliationWorker.schedule(this)
    }
}
