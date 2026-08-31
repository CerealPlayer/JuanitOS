package com.juanitos.testing

import androidx.test.core.app.ApplicationProvider
import com.juanitos.JuanitOSApplication

/**
 * Robolectric's default device config is a tiny 320x470dp screen. On it, form screens with more
 * than a couple of fields silently collapse off-screen content to zero-height nodes, which makes
 * `performClick()` land on the wrong (visually overlapping, zero-size) node instead of throwing.
 * Apply via `@Config(qualifiers = TEST_DEVICE_QUALIFIERS)` on any test that renders a full screen.
 */
const val TEST_DEVICE_QUALIFIERS = "w411dp-h891dp"

/**
 * Installs a fresh [TestAppContainer] (in-memory DB) into the Robolectric [JuanitOSApplication],
 * replacing the real disk-backed container [JuanitOSApplication.onCreate] set up. Call this from
 * `@Before`, then launch the activity under test (e.g.
 * `ActivityScenario.launch(MainActivity::class.java)`) *after* this returns, so
 * `AppViewModelProvider` resolves ViewModels against the test repositories. Using
 * `ActivityScenario.launch` directly (rather than `createAndroidComposeRule<MainActivity>()`,
 * which launches the activity as part of rule setup, before `@Before` runs) is what guarantees the
 * ordering.
 */
fun installTestAppContainer(): TestAppContainer {
    val app = ApplicationProvider.getApplicationContext<JuanitOSApplication>()
    val container = TestAppContainer(app)
    app.container = container
    return container
}
