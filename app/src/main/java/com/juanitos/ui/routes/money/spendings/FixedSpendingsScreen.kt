package com.juanitos.ui.routes.money.spendings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.icons.Add
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import com.juanitos.ui.routes.money.FixedSpendingCard

object FixedSpendingsDestination : NavigationDestination {
    override val route = Routes.FixedSpending
    override val titleRes = R.string.fixed_spendings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedSpendingsScreen(
    onNavigateUp: () -> Unit,
    onNewFixedSpending: () -> Unit,
    viewModel: FixedSpendingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val fixedSpendings = viewModel.uiState.collectAsState().value.fixedSpendings
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(FixedSpendingsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {},
                floatingActionButton = {
                    FloatingActionButton(onClick = onNewFixedSpending) {
                        Add()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small)
                ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            items(fixedSpendings) { spending ->
                FixedSpendingCard(
                    fixedSpendingWithCategory = spending,
                    onFixedSpendingCheck = { enabled: Boolean ->
                        viewModel.toggleFixedSpendingEnabled(
                            spending.fixedSpending.id,
                            enabled
                        )
                    },
                    onDelete = { viewModel.deleteFixedSpending(it.fixedSpending) }
                )
            }
        }
    }
}

