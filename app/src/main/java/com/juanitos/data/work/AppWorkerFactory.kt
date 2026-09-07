package com.juanitos.data.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.juanitos.data.AppContainer

/**
 * Manual-DI substitute for Hilt's HiltWorkerFactory: this app wires everything from
 * [AppContainer] rather than a DI framework, so Workers are constructed the same way.
 */
class AppWorkerFactory(private val container: AppContainer) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        SavingsGoalRiskWorker::class.java.name -> SavingsGoalRiskWorker(
            context = appContext,
            params = workerParameters,
            savingsGoalRepository = container.savingsGoalRepository,
            monthlySummaryRepository = container.monthlySummaryRepository,
            accountRepository = container.accountRepository,
        )

        else -> null
    }
}
