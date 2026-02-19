package com.juanitos.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanitos.JuanitOSApplication
import com.juanitos.ui.routes.money.MoneyViewModel
import com.juanitos.ui.routes.money.categories.CategoriesViewModel
import com.juanitos.ui.routes.money.categories.NewCategoryViewModel
import com.juanitos.ui.routes.money.spendings.FixedSpendingsViewModel
import com.juanitos.ui.routes.money.spendings.NewFixedSpendingViewModel
import com.juanitos.ui.routes.money.transactions.NewTransactionViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MoneyViewModel(
                juanitOSApplication().container.cycleRepository
            )
        }
        initializer {
            com.juanitos.ui.routes.money.settings.MoneySettingsViewModel(
                juanitOSApplication().container.cycleRepository
            )
        }
        initializer {
            NewTransactionViewModel(
                juanitOSApplication().container.transactionRepository,
                juanitOSApplication().container.cycleRepository
            )
        }
        initializer {
            FixedSpendingsViewModel(
                juanitOSApplication().container.fixedSpendingRepository
            )
        }
        initializer {
            NewFixedSpendingViewModel(
                juanitOSApplication().container.fixedSpendingRepository,
                juanitOSApplication().container.cycleRepository
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
