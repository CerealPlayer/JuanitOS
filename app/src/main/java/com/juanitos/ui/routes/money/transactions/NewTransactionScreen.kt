package com.juanitos.ui.routes.money.transactions

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.DateField
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.commons.FrequencySelector
import com.juanitos.ui.commons.categories_search.CategoriesSearch
import com.juanitos.ui.commons.credit_card_search.CreditCardSearch
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
    onNavigateUp: () -> Unit,
    onNewCategory: () -> Unit,
    onNewCreditCard: () -> Unit,
    viewModel: NewTransactionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    val amountFocusRequester = remember { FocusRequester() }
    val categoryFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        amountFocusRequester.requestFocus()
    }

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewTransactionDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp
        )
    }) { innerPadding ->
        FormColumn(innerPadding) {
            OutlinedTextField(
                value = uiState.amountInput,
                onValueChange = { viewModel.setAmountInput(it) },
                label = { Text(text = stringResource(R.string.amount)) },
                isError = !uiState.isAmountValid,
                supportingText = {
                    if (!uiState.isAmountValid) {
                        Text(text = stringResource(R.string.amount_must_be_positive))
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(amountFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { categoryFocusRequester.requestFocus() }
                )
            )
            TransactionTypeSelector(
                isIncome = uiState.isIncome,
                onIsIncomeChange = { viewModel.setIsIncome(it) }
            )
            CategoriesSearch(
                categories = uiState.categories,
                onItemSelect = { viewModel.setCategoryId(it.id) },
                onAddCategory = onNewCategory,
                categoryFocusRequester = categoryFocusRequester,
                nextFieldFocusRequester = descriptionFocusRequester
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.isPaidWithCredit,
                    onCheckedChange = { viewModel.setIsPaidWithCredit(it) }
                )
                Text(text = stringResource(R.string.paid_with_credit))
            }
            if (uiState.isPaidWithCredit) {
                CreditCardSearch(
                    creditCards = uiState.creditCards,
                    onItemSelect = { viewModel.setCreditCardId(it.id) },
                    onAddCreditCard = onNewCreditCard
                )
            }
            OutlinedTextField(
                value = uiState.descriptionInput,
                onValueChange = { viewModel.setDescriptionInput(it) },
                label = { Text(text = stringResource(R.string.description_optional)) },
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(descriptionFocusRequester),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.saveTransaction(onSuccess = onNavigateUp) }
                )
            )
            DateField(
                label = stringResource(R.string.date),
                dateInput = uiState.dateInput,
                onDateSelected = { viewModel.setDateInput(it) }
            )
            FrequencySelector(
                frequency = uiState.frequency,
                onFrequencySelected = { viewModel.setFrequency(it) }
            )
            if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage, color = Color.Red)
            }
            Button(
                onClick = {
                    viewModel.saveTransaction(onSuccess = onNavigateUp)
                },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun TransactionTypeSelector(
    isIncome: Boolean,
    onIsIncomeChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .selectable(
                    selected = !isIncome,
                    onClick = { onIsIncomeChange(false) },
                    role = Role.RadioButton
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = !isIncome, onClick = { onIsIncomeChange(false) })
            Text(text = stringResource(R.string.transaction_type_expense))
        }
        Row(
            modifier = Modifier
                .selectable(
                    selected = isIncome,
                    onClick = { onIsIncomeChange(true) },
                    role = Role.RadioButton
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isIncome, onClick = { onIsIncomeChange(true) })
            Text(text = stringResource(R.string.transaction_type_income))
        }
    }
}
