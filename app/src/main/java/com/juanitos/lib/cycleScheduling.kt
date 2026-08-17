package com.juanitos.lib

import java.time.LocalDate
import java.time.YearMonth

sealed interface CycleReconciliationAction {
    data object NoAction : CycleReconciliationAction
    data class Bootstrap(val startDate: LocalDate, val amount: Double) : CycleReconciliationAction
    data class Rollover(
        val endDate: LocalDate,
        val newStartDate: LocalDate,
        val amount: Double,
    ) : CycleReconciliationAction
}

data class OpenCycleInfo(val id: Int, val startDate: LocalDate?)

/**
 * The start of the current scheduled period: this month's clamped day-of-month if `today`
 * is on or after it, otherwise last month's.
 */
fun scheduledPeriodStart(today: LocalDate, dayOfMonth: Int): LocalDate {
    val thisMonth = clampDayOfMonth(YearMonth.from(today), dayOfMonth)
    return if (today.isBefore(thisMonth)) {
        clampDayOfMonth(YearMonth.from(today).minusMonths(1), dayOfMonth)
    } else {
        thisMonth
    }
}

/**
 * Idempotent: once the open cycle's start reaches `periodStart`, this always returns NoAction,
 * so calling it repeatedly (e.g. every app foreground) never creates duplicate cycles. If the
 * app skips several months, this jumps straight to the current period instead of synthesizing
 * one cycle per missed month.
 */
fun computeReconciliationAction(
    scheduleDayOfMonth: Int?,
    scheduleAmount: Double?,
    openCycle: OpenCycleInfo?,
    today: LocalDate = LocalDate.now(),
): CycleReconciliationAction {
    if (scheduleDayOfMonth == null || scheduleAmount == null) return CycleReconciliationAction.NoAction
    val periodStart = scheduledPeriodStart(today, scheduleDayOfMonth)

    if (openCycle == null) {
        return CycleReconciliationAction.Bootstrap(periodStart, scheduleAmount)
    }
    // Corrupt/unparseable start_date: do nothing rather than risk a duplicate open cycle.
    val cycleStart = openCycle.startDate ?: return CycleReconciliationAction.NoAction
    return if (cycleStart < periodStart) {
        CycleReconciliationAction.Rollover(periodStart, periodStart, scheduleAmount)
    } else {
        CycleReconciliationAction.NoAction
    }
}
