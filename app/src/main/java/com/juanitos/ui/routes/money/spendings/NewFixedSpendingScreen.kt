package com.juanitos.ui.routes.money.spendings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.juanitos.R
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object NewFixedSpendingDestination : NavigationDestination {
    override val route = Routes.NewFixedSpending
    override val titleRes = R.string.new_fixed_spending
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFixedSpendingScreen(
    onNavigateUp: () -> Unit
) {
    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewFixedSpendingDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Pantalla para crear gasto fijo")
        }
    }
}

