package com.juanitos

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.juanitos.data.AppContainer
import com.juanitos.data.AppDataContainer
import com.juanitos.data.work.AppWorkerFactory
import com.juanitos.data.work.SavingsGoalRiskWorker
import com.juanitos.lib.notifications.createSavingsGoalNotificationChannel
import java.util.concurrent.TimeUnit

class JuanitOSApplication : Application(), Configuration.Provider {
    lateinit var container: AppContainer

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(AppWorkerFactory(container))
            .build()

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        createSavingsGoalNotificationChannel(this)
        scheduleSavingsGoalRiskCheck()
    }

    private fun scheduleSavingsGoalRiskCheck() {
        val request = PeriodicWorkRequestBuilder<SavingsGoalRiskWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SAVINGS_GOAL_RISK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    companion object {
        private const val SAVINGS_GOAL_RISK_WORK_NAME = "savings_goal_risk_check"
    }
}
