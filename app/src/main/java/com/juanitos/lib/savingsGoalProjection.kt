package com.juanitos.lib

import com.juanitos.data.money.entities.SavingsGoal
import java.time.LocalDate

/** Projected pace toward a [SavingsGoal] for the current month. */
enum class SavingsGoalPaceStatus { ON_TRACK, AT_RISK, AHEAD, NO_TARGET }

/** Projected pace crosses [AT_RISK] when on track to miss the target by more than this fraction. */
const val AT_RISK_THRESHOLD = 0.8

data class SavingsGoalProjection(
    val target: Double,
    val savedSoFar: Double,
    val projected: Double,
    val safeToSpendToday: Double,
    val status: SavingsGoalPaceStatus,
)

/**
 * Linearly projects [savedSoFar] (this month's net savings, i.e. income - expenses) to month-end
 * based on the average daily rate so far, and derives a safe-to-spend-today figure and pace
 * status against [target].
 *
 * [target] <= 0 (unset, or a percentage goal with no prior-month income yet) short-circuits to
 * [SavingsGoalPaceStatus.NO_TARGET] rather than a misleading 0%-progress read.
 */
fun computeSavingsGoalProjection(
    target: Double,
    savedSoFar: Double,
    today: LocalDate = LocalDate.now(),
): SavingsGoalProjection {
    if (target <= 0.0) {
        return SavingsGoalProjection(
            target = 0.0,
            savedSoFar = savedSoFar,
            projected = savedSoFar,
            safeToSpendToday = 0.0,
            status = SavingsGoalPaceStatus.NO_TARGET,
        )
    }

    val daysElapsed = daysElapsedInMonth(today) // always >= 1, no div-by-zero on day 1
    val daysInMonth = today.lengthOfMonth()
    val daysRemaining = daysInMonth - daysElapsed

    val projected = savedSoFar / daysElapsed * daysInMonth
    val safeToSpendToday = if (daysRemaining <= 0) 0.0 else (target - savedSoFar) / daysRemaining
    val status = when {
        projected < target * AT_RISK_THRESHOLD -> SavingsGoalPaceStatus.AT_RISK
        projected > target -> SavingsGoalPaceStatus.AHEAD
        else -> SavingsGoalPaceStatus.ON_TRACK
    }

    return SavingsGoalProjection(
        target = target,
        savedSoFar = savedSoFar,
        projected = projected,
        safeToSpendToday = safeToSpendToday,
        status = status,
    )
}

/**
 * Resolves [goal]'s € target for the current month. FIXED goals use [SavingsGoal.fixedAmount]
 * directly; PERCENTAGE goals are based on [previousMonthIncome] (stable and known from day 1 of
 * the month, unlike the current month's still-accruing income). Returns 0.0 (surfaced by
 * [computeSavingsGoalProjection] as [SavingsGoalPaceStatus.NO_TARGET]) if there's no prior-month
 * income yet, e.g. a brand-new account.
 */
fun resolveGoalTarget(goal: SavingsGoal, previousMonthIncome: Double): Double =
    when (GoalType.valueOf(goal.goalType)) {
        GoalType.FIXED -> goal.fixedAmount ?: 0.0
        GoalType.PERCENTAGE ->
            if (previousMonthIncome <= 0.0) 0.0 else previousMonthIncome * (goal.percentage
                ?: 0.0) / 100.0
    }
