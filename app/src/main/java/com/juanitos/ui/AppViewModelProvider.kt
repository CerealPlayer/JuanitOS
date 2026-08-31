package com.juanitos.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanitos.JuanitOSApplication
import com.juanitos.ui.routes.money.MoneyViewModel
import com.juanitos.ui.routes.money.accounts.AccountsViewModel
import com.juanitos.ui.routes.money.accounts.NewAccountViewModel
import com.juanitos.ui.routes.money.categories.CategoriesViewModel
import com.juanitos.ui.routes.money.categories.NewCategoryViewModel
import com.juanitos.ui.routes.money.log.LogViewModel
import com.juanitos.ui.routes.money.stats.MoneyStatsViewModel
import com.juanitos.ui.routes.money.transactions.NewTransactionViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MoneyViewModel(
                juanitOSApplication().container.accountRepository,
                juanitOSApplication().container.transactionRepository,
            )
        }
        initializer {
            LogViewModel(
                juanitOSApplication().container.accountRepository,
                juanitOSApplication().container.transactionRepository,
                juanitOSApplication().container.categoryRepository
            )
        }
        initializer {
            MoneyStatsViewModel(
                juanitOSApplication().container.accountRepository,
            )
        }
        initializer {
            AccountsViewModel(
                juanitOSApplication().container.accountRepository
            )
        }
        initializer {
            NewAccountViewModel(
                juanitOSApplication().container.accountRepository
            )
        }
        initializer {
            NewTransactionViewModel(
                juanitOSApplication().container.transactionRepository,
                juanitOSApplication().container.accountRepository,
                juanitOSApplication().container.categoryRepository
            )
        }
        initializer {
            CategoriesViewModel(
                juanitOSApplication().container.categoryRepository
            )
        }
        initializer {
            NewCategoryViewModel(
                juanitOSApplication().container.categoryRepository
            )
        }
    }
}

fun CreationExtras.juanitOSApplication(): JuanitOSApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as JuanitOSApplication)
