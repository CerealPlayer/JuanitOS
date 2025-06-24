package com.juanitos.ui.routes.money

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
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
    onNavigateUp: () -> Unit,
    onMoneySettings: () -> Unit,
    onNewTransaction: () -> Unit,
    onNewFixedSpending: () -> Unit,
    viewModel: MoneyViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val uiState = viewModel.uiState.collectAsState()
    val cycle = uiState.value.cycle
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(MoneyDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = onMoneySettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings),
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = onNewFixedSpending) {
                        Icon(
                            painter = painterResource(R.drawable.fixed_spending),
                            contentDescription = stringResource(R.string.new_fixed_spending)
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = onNewTransaction) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.new_transaction)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small)
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            Text(
                text = "Money Screen",
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
            )
        }
    }
}