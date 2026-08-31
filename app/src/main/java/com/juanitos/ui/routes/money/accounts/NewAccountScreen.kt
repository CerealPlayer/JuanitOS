package com.juanitos.ui.routes.money.accounts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object NewAccountDestination : NavigationDestination {
    override val route = Routes.NewAccount
    override val titleRes = R.string.new_account
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccountScreen(
    onNavigateUp: () -> Unit,
    viewModel: NewAccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewAccountDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp
        )
    }) { innerPadding ->
        FormColumn(innerPadding) {
            OutlinedTextField(
                value = uiState.nameInput,
                onValueChange = { viewModel.setNameInput(it) },
                label = { Text(text = stringResource(R.string.account_name_label)) },
                isError = !uiState.isNameValid,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.startingBalanceInput,
                onValueChange = { viewModel.setStartingBalanceInput(it) },
                label = { Text(text = stringResource(R.string.starting_balance_label)) },
                isError = !uiState.isStartingBalanceValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = Color.Red
                )
            }
            Button(
                onClick = {
                    viewModel.saveAccount(onNavigateUp)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
