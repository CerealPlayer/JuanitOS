package com.juanitos.data.money.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.juanitos.JuanitOSApplication
import com.juanitos.lib.reconcileCycleWithSchedule
import java.util.concurrent.TimeUnit

/**
 * Daily check that ends/starts cycles to match the configured income schedule, so the cycle
 * boundary happens on the scheduled day even if the user never opens the Settings screen.
 */
class CycleReconciliationWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as JuanitOSApplication).container
        return try {
            reconcileCycleWithSchedule(
                container.cycleRepository,
                container.incomeScheduleRepository
            )
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "cycle_reconciliation"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<CycleReconciliationWorker>(1, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
