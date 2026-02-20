package com.juanitos.ui.routes.money

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.juanitos.data.money.entities.relations.TransactionWithCategory
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.icons.Add
import com.juanitos.ui.icons.Settings
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import java.util.Locale

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
    onFixedSpendings: () -> Unit,
    onCategories: () -> Unit,
    viewModel: MoneyViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val uiState = viewModel.uiState.collectAsState()
    val summary = uiState.value.summary
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(MoneyDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
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
                    IconButton(onClick = onFixedSpendings) {
                        Icon(
                            painter = painterResource(R.drawable.fixed_spending),
                            contentDescription = stringResource(R.string.new_fixed_spending)
                        )
                    }
                    IconButton(onClick = onCategories) {
                        Icon(
                            painter = painterResource(R.drawable.categories),
                            contentDescription = stringResource(R.string.categories)
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
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small)
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            // Summary box
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.inversePrimary)
                    .padding(dimensionResource(R.dimen.padding_medium))
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.total_income))
                    Text(text = String.format(Locale.US, "%.2f", summary.totalIncome))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.total_fixed_spendings))
                    Text(text = String.format(Locale.US, "%.2f", summary.totalFixedSpendings))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.total_transactions))
                    Text(text = String.format(Locale.US, "%.2f", summary.totalTransactions))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.remaining_money))
                    Text(text = String.format(Locale.US, "%.2f", summary.remaining))
                }
            }
            val transactions = uiState.value.cycle?.transactions ?: emptyList()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.padding_medium)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                // Luego mostrar transacciones
                items(transactions) { transaction ->
                    TransactionCard(transactionWithCategory = transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    transactionWithCategory: TransactionWithCategory,
    modifier: Modifier = Modifier
) {
    val transaction = transactionWithCategory.transaction
    val category = transactionWithCategory.category
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Text(text = String.format(Locale.US, "%.2f€", transaction.amount))
                Text(
                    text = category?.name ?: stringResource(R.string.uncategorized),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (transaction.description != null) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small))
                )
            }
        }
    }
}