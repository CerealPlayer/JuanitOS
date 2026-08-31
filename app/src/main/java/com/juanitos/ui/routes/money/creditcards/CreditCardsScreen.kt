package com.juanitos.ui.routes.money.creditcards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.money.entities.CreditCard
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.icons.Add
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object CreditCardsDestination : NavigationDestination {
    override val route = Routes.CreditCards
    override val titleRes = R.string.credit_cards
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardsScreen(
    onNavigateUp: () -> Unit,
    onNewCreditCard: () -> Unit,
    viewModel: CreditCardsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(CreditCardsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {},
                floatingActionButton = {
                    FloatingActionButton(onClick = onNewCreditCard) {
                        Add()
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.creditCards.isEmpty()) {
            Text(
                text = stringResource(R.string.no_credit_cards),
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding(),
                    start = dimensionResource(R.dimen.padding_medium),
                    end = dimensionResource(R.dimen.padding_medium)
                )
            )
        } else {
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
                items(uiState.creditCards) { creditCard ->
                    val accountName =
                        uiState.accounts.firstOrNull { it.id == creditCard.accountId }?.name ?: ""
                    CreditCardCard(creditCard = creditCard, accountName = accountName)
                }
            }
        }
    }
}

@Composable
fun CreditCardCard(creditCard: CreditCard, accountName: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_small))
        ) {
            Text(text = creditCard.name, fontWeight = FontWeight.Bold)
            Text(text = accountName)
            Text(text = stringResource(R.string.credit_card_payment_day, creditCard.paymentDay))
        }
    }
}
