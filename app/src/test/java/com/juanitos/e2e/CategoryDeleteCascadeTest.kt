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
 * [com.juanitos.data.money.entities.Transaction] declares its foreign key to
 * [com.juanitos.data.money.entities.Category] with `onDelete = ForeignKey.CASCADE`, and there is no
 * confirmation UI in front of category deletion. That means deleting a category silently deletes
 * every transaction that used it - a real, irreversible data-loss trap. This test documents that
 * current behavior as a regression guard (it will fail loudly the moment someone tries to make
 * category deletion safer without also updating this test).
 */
@RunWith(AndroidJUnit4::class)
@Config(application = JuanitOSApplication::class)
class CategoryDeleteCascadeTest {
    private lateinit var container: TestAppContainer

    @Before
    fun setUp() {
        container = installTestAppContainer()
    }

    @Test
    fun deletingCategory_cascadesToDeleteItsTransactions() = runBlocking {
        val accountId = TestSeed.account(container, name = "Main", startingBalance = 100.0)
        val categoryId = TestSeed.category(container, name = "One-off Category")
        TestSeed.transaction(container, accountId, categoryId, amount = 25.0)

        val beforeDelete = container.accountRepository.getSelected().first()
        assertThat(beforeDelete?.transactions).hasSize(1)

        val categoryToDelete = container.categoryRepository.getById(categoryId).first()
        container.categoryRepository.delete(categoryToDelete)

        val afterDelete = container.accountRepository.getSelected().first()
        assertThat(afterDelete?.transactions).isEmpty()
    }
}
