package com.juanitos.ui.routes.money

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.juanitos.ui.icons.Add
import com.juanitos.ui.icons.Settings
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
    onMoneySettings: () -> Unit,
    onNewTransaction: () -> Unit,
    onFixedSpendings: () -> Unit,
    onCategories: () -> Unit,
    onMoneyStats: () -> Unit,
    onLog: () -> Unit,
    viewModel: MoneyViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val uiState = viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(MoneyDestination.titleRes),
                canNavigateBack = false,
                actions = {
                    IconButton(onClick = onMoneySettings) {
                        Settings()
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = onLog) {
                        Icon(
                            painter = painterResource(R.drawable.categories),
                            contentDescription = stringResource(R.string.log)
                        )
                    }
                    IconButton(onClick = onFixedSpendings) {
                        Icon(
                            painter = painterResource(R.drawable.fixed_spending),
                            contentDescription = stringResource(R.string.new_fixed_spending)
                        )
                    }
                    IconButton(onClick = onCategories) {
                        Icon(
                            painter = painterResource(R.drawable.category),
                            contentDescription = stringResource(R.string.categories)
                        )
                    }
                    IconButton(onClick = onMoneyStats) {
                        Icon(
                            painter = painterResource(R.drawable.statistics),
                            contentDescription = stringResource(R.string.money_stats)
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = onNewTransaction) {
                        Add()
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
                    start = dimensionResource(R.dimen.padding_medium),
                    end = dimensionResource(R.dimen.padding_medium)
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            val summary = uiState.value.summary
            if (summary != null) {
                CycleSummary(
                    summary = summary,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_medium))
                )
            } else {
                Text(text = stringResource(R.string.money_stats_no_active_cycle))
            }
        }
    }
}
