package com.juanitos.lib

import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.IncomeScheduleRepository
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
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
 * Bank-style "paycheck doesn't land on a non-business day" adjustment: Saturday moves forward to
 * Monday, Sunday moves forward to Monday. Banks generally wire income after the weekend rather
 * than before it.
 */
fun adjustForBankingWeekend(date: LocalDate): LocalDate = when (date.dayOfWeek) {
    DayOfWeek.SATURDAY -> date.plusDays(2)
    DayOfWeek.SUNDAY -> date.plusDays(1)
    else -> date
}

/**
 * The start of the current scheduled period: this month's clamped day-of-month if `today`
 * is on or after it, otherwise last month's.
 */
fun scheduledPeriodStart(
    today: LocalDate,
    dayOfMonth: Int,
    adjustForWeekends: Boolean = false,
): LocalDate {
    val clamp = { month: YearMonth ->
        val date = clampDayOfMonth(month, dayOfMonth)
        if (adjustForWeekends) adjustForBankingWeekend(date) else date
    }
    val thisMonth = clamp(YearMonth.from(today))
    return if (today.isBefore(thisMonth)) {
        clamp(YearMonth.from(today).minusMonths(1))
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
    adjustForWeekends: Boolean = false,
): CycleReconciliationAction {
    if (scheduleDayOfMonth == null || scheduleAmount == null) return CycleReconciliationAction.NoAction
    val periodStart = scheduledPeriodStart(today, scheduleDayOfMonth, adjustForWeekends)

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

/**
 * Reads the current schedule and open cycle once and materializes/rolls over a [Cycle][
 * com.juanitos.data.money.entities.Cycle] row to match, if needed. Safe to call repeatedly
 * (e.g. on every screen that can create or edit the schedule) since [computeReconciliationAction]
 * is idempotent.
 */
suspend fun reconcileCycleWithSchedule(
    cycleRepository: CycleRepository,
    incomeScheduleRepository: IncomeScheduleRepository,
) {
    val schedule = incomeScheduleRepository.get().first()
    val openCycle = cycleRepository.getCurrentCycle().first()?.cycle
    val openCycleInfo = openCycle?.let {
        OpenCycleInfo(id = it.id, startDate = parseDbDatetimeToLocalDate(it.startDate))
    }
    when (
        val action = computeReconciliationAction(
            scheduleDayOfMonth = schedule?.dayOfMonth,
            scheduleAmount = schedule?.amount,
            openCycle = openCycleInfo,
            adjustForWeekends = schedule?.adjustForWeekends ?: false,
        )
    ) {
        is CycleReconciliationAction.NoAction -> {}
        is CycleReconciliationAction.Bootstrap -> {
            cycleRepository.insert(action.amount, formatLocalDateToDbDatetime(action.startDate))
        }

        is CycleReconciliationAction.Rollover -> {
            openCycle?.let { cycle ->
                cycleRepository.update(cycle.copy(endDate = formatLocalDateToDbDatetime(action.endDate)))
            }
            cycleRepository.insert(
                action.amount,
                formatLocalDateToDbDatetime(action.newStartDate)
            )
        }
    }
}
