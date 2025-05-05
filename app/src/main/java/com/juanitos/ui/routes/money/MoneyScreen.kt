package com.juanitos.ui.routes.money

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.juanitos.R
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object MoneyDestination : NavigationDestination {
    override val route = Routes.Money
    override val titleRes = R.string.money
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyScreen(
    onNavigateUp: () -> Unit
) {
    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(MoneyDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp
        )
    }) {
            innerPadding ->
        Text("Money", modifier = Modifier.padding(innerPadding))
    }
}