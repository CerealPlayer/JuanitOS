package com.juanitos.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.juanitos.JuanitOSApplication
import com.juanitos.testing.TestAppContainer
import com.juanitos.testing.TestSeed
import com.juanitos.testing.installTestAppContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * [com.juanitos.data.money.daos.AccountDao] has a plain `@Delete`, with no query that re-selects a
 * remaining account afterward. There is also no delete UI wired up on
 * [com.juanitos.ui.routes.money.accounts.AccountsScreen] today, so these exercise
 * [com.juanitos.data.money.repositories.AccountRepository.delete] directly - real production code,
 * just without a UI entry point yet.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = JuanitOSApplication::class)
class SelectedAccountDeleteTest {
    private lateinit var container: TestAppContainer

    @Before
    fun setUp() {
        container = installTestAppContainer()
    }

    @Test
    fun deletingTheOnlyAccount_leavesNoAccountSelected() = runBlocking {
        val accountId = TestSeed.account(container, name = "Solo")

        val account = container.accountRepository.getAll().first().single { it.id == accountId }
        container.accountRepository.delete(account)

        assertThat(container.accountRepository.getSelected().first()).isNull()
    }

    @Test
    fun deletingTheSelectedAccount_doesNotAutoSelectAnotherAccount() = runBlocking {
        val firstAccountId = TestSeed.account(container, name = "First")
        // Inserted directly (not via TestSeed.account, which always selects) so it stays unselected.
        container.accountRepository.insert("Second", 0.0)

        val selectedAccount =
            container.accountRepository.getAll().first().single { it.id == firstAccountId }
        container.accountRepository.delete(selectedAccount)

        // Documents the current gap: no account is auto-selected after the selected one is
        // deleted, even though a valid remaining account ("Second") exists.
        assertThat(container.accountRepository.getSelected().first()).isNull()
    }
}
