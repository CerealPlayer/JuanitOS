package com.juanitos.lib

import com.juanitos.data.money.entities.SavingsGoal
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SavingsGoalProjectionTest {

    @Test
    fun computeSavingsGoalProjection_returnsNoTargetWhenTargetIsZeroOrUnset() {
        val result = computeSavingsGoalProjection(
            target = 0.0,
            savedSoFar = 50.0,
            today = LocalDate.of(2026, 6, 15)
        )
        assertEquals(SavingsGoalPaceStatus.NO_TARGET, result.status)
        assertEquals(0.0, result.safeToSpendToday, 0.0001)
    }

    @Test
    fun computeSavingsGoalProjection_doesNotDivideByZeroOnFirstDayOfMonth() {
        val result = computeSavingsGoalProjection(
            target = 300.0,
            savedSoFar = 10.0,
            today = LocalDate.of(2026, 6, 1)
        )
        // daysElapsed = 1, daysInMonth = 30 -> projected = 10 * 30 = 300
        assertEquals(300.0, result.projected, 0.0001)
    }

    @Test
    fun computeSavingsGoalProjection_safeToSpendTodayIsZeroOnLastDayOfMonth() {
        val result = computeSavingsGoalProjection(
            target = 300.0,
            savedSoFar = 100.0,
            today = LocalDate.of(2026, 6, 30)
        )
        assertEquals(0.0, result.safeToSpendToday, 0.0001)
    }

    @Test
    fun computeSavingsGoalProjection_atRiskWhenProjectedMissesTargetByMoreThanThreshold() {
        // Halfway through a 30-day month, saved 50 of a 300 target -> projected 100, well under 80% of 300.
        val result = computeSavingsGoalProjection(
            target = 300.0,
            savedSoFar = 50.0,
            today = LocalDate.of(2026, 6, 15)
        )
        assertEquals(SavingsGoalPaceStatus.AT_RISK, result.status)
    }

    @Test
    fun computeSavingsGoalProjection_onTrackWhenProjectedIsCloseToTarget() {
        // Halfway through a 30-day month, saved 145 of a 300 target -> projected 290, within [240, 300].
        val result = computeSavingsGoalProjection(
            target = 300.0,
            savedSoFar = 145.0,
            today = LocalDate.of(2026, 6, 15)
        )
        assertEquals(SavingsGoalPaceStatus.ON_TRACK, result.status)
    }

    @Test
    fun computeSavingsGoalProjection_aheadWhenProjectedExceedsTarget() {
        val result = computeSavingsGoalProjection(
            target = 300.0,
            savedSoFar = 200.0,
            today = LocalDate.of(2026, 6, 15)
        )
        assertEquals(SavingsGoalPaceStatus.AHEAD, result.status)
    }

    @Test
    fun computeSavingsGoalProjection_handlesNegativeSavedSoFarAsAtRisk() {
        val result = computeSavingsGoalProjection(
            target = 300.0,
            savedSoFar = -50.0,
            today = LocalDate.of(2026, 6, 15)
        )
        assertEquals(SavingsGoalPaceStatus.AT_RISK, result.status)
    }

    @Test
    fun resolveGoalTarget_returnsFixedAmountForFixedGoal() {
        val goal = SavingsGoal(accountId = 1, goalType = GoalType.FIXED.name, fixedAmount = 200.0)
        assertEquals(200.0, resolveGoalTarget(goal, previousMonthIncome = 0.0), 0.0001)
    }

    @Test
    fun resolveGoalTarget_computesPercentageOfPreviousMonthIncome() {
        val goal =
            SavingsGoal(accountId = 1, goalType = GoalType.PERCENTAGE.name, percentage = 10.0)
        assertEquals(150.0, resolveGoalTarget(goal, previousMonthIncome = 1500.0), 0.0001)
    }

    @Test
    fun resolveGoalTarget_returnsZeroForPercentageGoalWithNoPriorIncome() {
        val goal =
            SavingsGoal(accountId = 1, goalType = GoalType.PERCENTAGE.name, percentage = 10.0)
        assertEquals(0.0, resolveGoalTarget(goal, previousMonthIncome = 0.0), 0.0001)
    }
}
