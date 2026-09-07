package com.juanitos.data.work

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.juanitos.data.money.repositories.AccountRepository
import com.juanitos.data.money.repositories.MonthlySummaryRepository
import com.juanitos.data.money.repositories.SavingsGoalRepository
import com.juanitos.lib.SavingsGoalPaceStatus
import com.juanitos.lib.computeSavingsGoalProjection
import com.juanitos.lib.notifications.sendSavingsGoalRiskNotification
import com.juanitos.lib.resolveGoalTarget
import kotlinx.coroutines.flow.first
import java.time.YearMonth

/**
 * Runs daily (see [com.juanitos.JuanitOSApplication]'s periodic work enqueue): for every account
 * with a savings goal that has notifications enabled, projects this month's pace and fires a
 * local notification the first time it crosses [SavingsGoalPaceStatus.AT_RISK] in a given month.
 */
class SavingsGoalRiskWorker(
    context: Context,
    params: WorkerParameters,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val monthlySummaryRepository: MonthlySummaryRepository,
    private val accountRepository: AccountRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val goals = savingsGoalRepository.getAllWithNotificationsEnabled()
        if (goals.isEmpty()) return Result.success()

        val accountNamesById =
            accountRepository.getAll().first().associateBy({ it.id }, { it.name })
        val currentMonth = YearMonth.now().toString()
        val previousMonth = YearMonth.now().minusMonths(1).toString()

        for (goal in goals) {
            if (goal.lastRiskNotifiedMonth == currentMonth) continue
            val accountName = accountNamesById[goal.accountId] ?: continue

            val previousSummary =
                monthlySummaryRepository.getByAccountAndMonth(goal.accountId, previousMonth)
            val currentSummary =
                monthlySummaryRepository.getByAccountAndMonth(goal.accountId, currentMonth)

            val target = resolveGoalTarget(goal, previousSummary?.totalIncome ?: 0.0)
            val savedSoFar = currentSummary?.netBalance ?: 0.0
            val projection = computeSavingsGoalProjection(target, savedSoFar)

            if (projection.status == SavingsGoalPaceStatus.AT_RISK) {
                sendSavingsGoalRiskNotification(
                    context = applicationContext,
                    notificationId = goal.accountId,
                    accountName = accountName,
                    projection = projection,
                )
                savingsGoalRepository.markRiskNotified(goal.id, currentMonth)
            }
        }

        return Result.success()
    }
}
