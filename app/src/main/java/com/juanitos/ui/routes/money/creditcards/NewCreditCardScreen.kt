package com.juanitos.ui.routes.money.creditcards

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.money.entities.Account
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object NewCreditCardDestination : NavigationDestination {
    override val route = Routes.NewCreditCard
    override val titleRes = R.string.new_credit_card
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCreditCardScreen(
    onNavigateUp: () -> Unit,
    viewModel: NewCreditCardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewCreditCardDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp
        )
    }) { innerPadding ->
        FormColumn(innerPadding) {
            OutlinedTextField(
                value = uiState.nameInput,
                onValueChange = { viewModel.setNameInput(it) },
                label = { Text(text = stringResource(R.string.credit_card_name_label)) },
                isError = !uiState.isNameValid,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            AccountSelector(
                accounts = uiState.accounts,
                selectedAccountId = uiState.accountId,
                onAccountSelected = { viewModel.setAccountId(it) }
            )
            OutlinedTextField(
                value = uiState.paymentDayInput,
                onValueChange = { viewModel.setPaymentDayInput(it) },
                label = { Text(text = stringResource(R.string.credit_card_payment_day_label)) },
                isError = !uiState.isPaymentDayValid,
                supportingText = {
                    if (!uiState.isPaymentDayValid) {
                        Text(text = stringResource(R.string.invalid_payment_day))
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage, color = Color.Red)
            }
            Button(
                onClick = { viewModel.saveCreditCard(onNavigateUp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSelector(
    accounts: List<Account>,
    selectedAccountId: Int?,
    onAccountSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedAccount?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.credit_card_account_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = {
                        onAccountSelected(account.id)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
