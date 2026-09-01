package com.juanitos.e2e

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.juanitos.JuanitOSApplication
import com.juanitos.MainActivity
import com.juanitos.R
import com.juanitos.testing.TEST_DEVICE_QUALIFIERS
import com.juanitos.testing.TestAppContainer
import com.juanitos.testing.installTestAppContainer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The core "normal user" journey: create an account, then log a transaction against it, and see
 * both reflected on the [com.juanitos.ui.routes.money.MoneyScreen] home dashboard.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = JuanitOSApplication::class, qualifiers = TEST_DEVICE_QUALIFIERS)
class AccountAndTransactionFlowTest {
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var container: TestAppContainer

    private fun string(id: Int) =
        ApplicationProvider.getApplicationContext<android.content.Context>().getString(id)

    @Before
    fun setUp() {
        container = installTestAppContainer()
    }

    @Test
    fun createAccount_thenTransaction_reflectsInHomeSummary() {
        // "Groceries" is one of the default categories SeedDefaultCategoriesCallback inserts into
        // every fresh database, so no category seeding is needed here.
        ActivityScenario.launch(MainActivity::class.java)
        composeTestRule.waitForIdle()

        // Money home starts empty -> go create an account.
        composeTestRule.onNodeWithText(string(R.string.money_stats_no_account)).assertExists()
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Add Icon").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(string(R.string.account_name_label))
            .performTextInput("Test Account")
        composeTestRule.onNodeWithText(string(R.string.starting_balance_label))
            .performTextInput("100")
        composeTestRule.onNodeWithText(string(R.string.skip_setup)).performClick()
        composeTestRule.waitForIdle()

        // Back on Accounts, the new (auto-selected) account is listed.
        composeTestRule.onNodeWithText("Test Account").assertExists()
        composeTestRule.onNodeWithContentDescription("Arrow Back Icon").performClick()
        composeTestRule.waitForIdle()

        // Home now shows the account and its starting balance.
        composeTestRule.onNodeWithText("Test Account").assertExists()
        composeTestRule.onNodeWithText("100.00€").assertExists()

        // Log a 40€ expense against it, categorized as Groceries.
        composeTestRule.onNodeWithContentDescription("Add Icon").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(string(R.string.amount)).performTextInput("40")
        composeTestRule.onNodeWithText("Category").performTextInput("Groc")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Groceries").performClick()
        composeTestRule.onNodeWithText(string(R.string.save)).performClick()
        composeTestRule.waitForIdle()

        // Home reflects the new remaining balance and the category breakdown.
        composeTestRule.onNodeWithText("60.00€").assertExists()
        composeTestRule.onNodeWithText("Groceries").assertExists()
        composeTestRule.onNodeWithText("40.00€").assertExists()
    }
}
