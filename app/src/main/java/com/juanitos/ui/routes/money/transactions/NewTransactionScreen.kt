package com.juanitos.ui.routes.money.transactions

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

object NewTransactionDestination : NavigationDestination {
    override val route = Routes.NewTransaction
    override val titleRes = R.string.new_transaction
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTransactionScreen(
    onNavigateUp: () -> Unit
) {
    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewTransactionDestination.titleRes),
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
            Text(text = "Pantalla para crear transacción")
        }
    }
}

