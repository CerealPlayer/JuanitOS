package com.juanitos.e2e

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.juanitos.MainActivity
import com.juanitos.R
import com.juanitos.testing.TEST_DEVICE_QUALIFIERS
import com.juanitos.testing.installTestAppContainer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Proves the Robolectric + Compose + in-memory-Room harness boots end to end before any real
 * flow test is written: a fresh app with no accounts shows [MoneyScreen]'s empty state.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = com.juanitos.JuanitOSApplication::class, qualifiers = TEST_DEVICE_QUALIFIERS)
class MoneyScreenSmokeTest {
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Before
    fun setUp() {
        installTestAppContainer()
    }

    @Test
    fun emptyDatabase_showsNoAccountSelectedState() {
        ActivityScenario.launch(MainActivity::class.java)

        val expectedText = ApplicationProvider.getApplicationContext<android.content.Context>()
            .getString(R.string.money_stats_no_account)
        composeTestRule.onNodeWithText(expectedText).assertExists()
    }
}
