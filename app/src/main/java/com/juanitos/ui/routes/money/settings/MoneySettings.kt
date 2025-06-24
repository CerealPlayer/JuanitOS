package com.juanitos.ui.routes.money.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object MoneySettingsDestination : NavigationDestination {
    override val route = Routes.MoneySettings
    override val titleRes = R.string.settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneySettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: MoneySettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(MoneySettingsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
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
            Text(text = stringResource(R.string.current_cycle))
            if (uiState.currentCycle != null) {
                Text(text = "Inicio: ${uiState.currentCycle?.startDate ?: "-"}")
                Text(text = "Ingreso: ${uiState.currentCycle?.totalIncome ?: 0.0}")
                Text(text = "Fin: ${uiState.currentCycle?.endDate ?: "(abierto)"}")
                Button(onClick = { viewModel.endCurrentCycle() }) {
                    Text(text = stringResource(R.string.end_cycle))
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    OutlinedTextField(
                        value = uiState.incomeInput,
                        onValueChange = { viewModel.setIncomeInput(it) },
                        singleLine = true,
                        isError = !uiState.isIncomeValid,
                        label = { Text(stringResource(R.string.income_input_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.createNewCycle(onNavigateUp) },
                        enabled = uiState.isIncomeValid && uiState.incomeInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.create_cycle))
                    }
                }
            }
            if (!uiState.errorMessage.isNullOrEmpty()) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = androidx.compose.ui.graphics.Color.Red
                )
            }
        }
    }
}