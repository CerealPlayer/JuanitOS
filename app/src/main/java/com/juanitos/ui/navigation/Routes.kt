package com.juanitos.ui.navigation

enum class Routes(val route: String) {
    Money("money"),
    MoneyStats("money_stats"),
    MoneySettings("money_settings"),
    NewTransaction("new_transaction"),
    FixedSpending("fixed_spending"),
    NewFixedSpending("new_fixed_spending"),
    EditFixedSpending("edit_fixed_spending/{fixedSpendingId}"),
    Categories("categories"),
    NewCategory("new_category");
}
