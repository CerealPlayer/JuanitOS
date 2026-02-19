package com.juanitos.ui.routes.money.spendings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.icons.Add
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import java.util.Locale

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
                FixedSpendingCard(fixedSpending = spending)
            }
        }
    }
}

@Composable
fun FixedSpendingCard(fixedSpending: FixedSpendingWithCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = fixedSpending.category.name)
                Text(text = String.format(Locale.US, "%.2f", fixedSpending.fixedSpending.amount))
            }
            if (!fixedSpending.fixedSpending.description.isNullOrBlank()) {
                Text(
                    text = fixedSpending.fixedSpending.description,
                )
            }
        }
    }
}