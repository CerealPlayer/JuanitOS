package com.juanitos.lib

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CycleSchedulingTest {

    @Test
    fun scheduledPeriodStart_returnsThisMonthWhenOnOrAfterDay() {
        assertEquals(
            LocalDate.of(2026, 8, 15),
            scheduledPeriodStart(today = LocalDate.of(2026, 8, 15), dayOfMonth = 15)
        )
        assertEquals(
            LocalDate.of(2026, 8, 15),
            scheduledPeriodStart(today = LocalDate.of(2026, 8, 20), dayOfMonth = 15)
        )
    }

    @Test
    fun scheduledPeriodStart_returnsLastMonthWhenBeforeDay() {
        assertEquals(
            LocalDate.of(2026, 7, 15),
            scheduledPeriodStart(today = LocalDate.of(2026, 8, 10), dayOfMonth = 15)
        )
    }

    @Test
    fun scheduledPeriodStart_clampsAcrossMonthBoundary() {
        // dayOfMonth 31 requested in Feb: today before Feb-28 -> falls back to clamped Jan 31.
        assertEquals(
            LocalDate.of(2026, 1, 31),
            scheduledPeriodStart(today = LocalDate.of(2026, 2, 10), dayOfMonth = 31)
        )
    }

    @Test
    fun adjustForBankingWeekend_movesSaturdayToNextMonday() {
        assertEquals(
            LocalDate.of(2026, 8, 17),
            adjustForBankingWeekend(LocalDate.of(2026, 8, 15))
        )
    }

    @Test
    fun adjustForBankingWeekend_movesSundayToNextMonday() {
        assertEquals(
            LocalDate.of(2026, 8, 17),
            adjustForBankingWeekend(LocalDate.of(2026, 8, 16))
        )
    }

    @Test
    fun adjustForBankingWeekend_leavesWeekdaysUnchanged() {
        assertEquals(
            LocalDate.of(2026, 8, 17),
            adjustForBankingWeekend(LocalDate.of(2026, 8, 17))
        )
    }

    @Test
    fun scheduledPeriodStart_adjustsForWeekendWhenEnabled() {
        // 2026-08-15 is a Saturday -> adjusted forward to Monday 2026-08-17.
        assertEquals(
            LocalDate.of(2026, 8, 17),
            scheduledPeriodStart(
                today = LocalDate.of(2026, 8, 20),
                dayOfMonth = 15,
                adjustForWeekends = true
            )
        )
    }

    @Test
    fun computeReconciliationAction_bootstrapsWithWeekendAdjustedDate() {
        // 2026-08-15 is a Saturday -> adjusted bootstrap date should be Monday 2026-08-17.
        val action = computeReconciliationAction(
            scheduleDayOfMonth = 15,
            scheduleAmount = 2500.0,
            openCycle = null,
            today = LocalDate.of(2026, 8, 20),
            adjustForWeekends = true
        )
        assertTrue(action is CycleReconciliationAction.Bootstrap)
        assertEquals(
            LocalDate.of(2026, 8, 17),
            (action as CycleReconciliationAction.Bootstrap).startDate
        )
    }

    @Test
    fun computeReconciliationAction_noActionWhenScheduleUnset() {
        val action = computeReconciliationAction(
            scheduleDayOfMonth = null,
            scheduleAmount = null,
            openCycle = null,
            today = LocalDate.of(2026, 8, 17)
        )
        assertEquals(CycleReconciliationAction.NoAction, action)
    }

    @Test
    fun computeReconciliationAction_bootstrapsWhenNoOpenCycle() {
        val action = computeReconciliationAction(
            scheduleDayOfMonth = 15,
            scheduleAmount = 2500.0,
            openCycle = null,
            today = LocalDate.of(2026, 8, 17)
        )
        assertTrue(action is CycleReconciliationAction.Bootstrap)
        val bootstrap = action as CycleReconciliationAction.Bootstrap
        assertEquals(LocalDate.of(2026, 8, 15), bootstrap.startDate)
        assertEquals(2500.0, bootstrap.amount, 0.0)
    }

    @Test
    fun computeReconciliationAction_rolloverWhenOpenCyclePredatesCurrentPeriod() {
        val openCycle = OpenCycleInfo(id = 1, startDate = LocalDate.of(2026, 7, 15))
        val action = computeReconciliationAction(
            scheduleDayOfMonth = 15,
            scheduleAmount = 2500.0,
            openCycle = openCycle,
            today = LocalDate.of(2026, 8, 20)
        )
        assertTrue(action is CycleReconciliationAction.Rollover)
        val rollover = action as CycleReconciliationAction.Rollover
        assertEquals(LocalDate.of(2026, 8, 15), rollover.endDate)
        assertEquals(LocalDate.of(2026, 8, 15), rollover.newStartDate)
    }

    @Test
    fun computeReconciliationAction_noActionWhenAlreadyCurrent() {
        val openCycle = OpenCycleInfo(id = 1, startDate = LocalDate.of(2026, 8, 15))
        val action = computeReconciliationAction(
            scheduleDayOfMonth = 15,
            scheduleAmount = 2500.0,
            openCycle = openCycle,
            today = LocalDate.of(2026, 8, 20)
        )
        assertEquals(CycleReconciliationAction.NoAction, action)
    }

    @Test
    fun computeReconciliationAction_isIdempotentAfterRollover() {
        // Simulate re-running immediately after a rollover: the new open cycle's start now
        // equals the period start, so a second call must be a no-op.
        val rolledOverCycle = OpenCycleInfo(id = 2, startDate = LocalDate.of(2026, 8, 15))
        val action = computeReconciliationAction(
            scheduleDayOfMonth = 15,
            scheduleAmount = 2500.0,
            openCycle = rolledOverCycle,
            today = LocalDate.of(2026, 8, 15)
        )
        assertEquals(CycleReconciliationAction.NoAction, action)
    }

    @Test
    fun computeReconciliationAction_noActionOnCorruptStartDate() {
        val openCycle = OpenCycleInfo(id = 1, startDate = null)
        val action = computeReconciliationAction(
            scheduleDayOfMonth = 15,
            scheduleAmount = 2500.0,
            openCycle = openCycle,
            today = LocalDate.of(2026, 8, 20)
        )
        assertEquals(CycleReconciliationAction.NoAction, action)
    }

    @Test
    fun computeReconciliationAction_noCatchUpFloodAfterMonthsAway() {
        // App last opened in April; schedule is day 1. Reopening in August must jump straight
        // to the current period, not synthesize May/June/July cycles.
        val openCycle = OpenCycleInfo(id = 1, startDate = LocalDate.of(2026, 4, 1))
        val action = computeReconciliationAction(
            scheduleDayOfMonth = 1,
            scheduleAmount = 2500.0,
            openCycle = openCycle,
            today = LocalDate.of(2026, 8, 17)
        )
        assertTrue(action is CycleReconciliationAction.Rollover)
        val rollover = action as CycleReconciliationAction.Rollover
        assertEquals(LocalDate.of(2026, 8, 1), rollover.newStartDate)
    }
}
