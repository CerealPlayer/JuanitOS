package com.juanitos.ui.routes.money.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.DateField
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.commons.FrequencySelector
import com.juanitos.ui.commons.categories_search.CategoriesSearch
import com.juanitos.ui.icons.Delete
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
    onNewCategory: () -> Unit,
    viewModel: NewAccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewAccountDestination.titleRes),
            canNavigateBack = true,
            navigateUp = {
                if (uiState.step == NewAccountStep.DETAILS) {
                    onNavigateUp()
                } else {
                    viewModel.previousStep()
                }
            }
        )
    }) { innerPadding ->
        FormColumn(innerPadding) {
            when (uiState.step) {
                NewAccountStep.DETAILS -> DetailsStep(
                    uiState = uiState,
                    viewModel = viewModel,
                    onNavigateUp = onNavigateUp
                )

                NewAccountStep.RECURRING_INCOME -> RecurringStep(
                    isIncome = true,
                    uiState = uiState,
                    viewModel = viewModel,
                    onNewCategory = onNewCategory,
                    onAdd = { viewModel.addIncome() },
                    onRemove = { viewModel.removeIncome(it) },
                    drafts = uiState.incomes,
                    onNext = { viewModel.goToExpenseStep() }
                )

                NewAccountStep.RECURRING_EXPENSE -> RecurringStep(
                    isIncome = false,
                    uiState = uiState,
                    viewModel = viewModel,
                    onNewCategory = onNewCategory,
                    onAdd = { viewModel.addExpense() },
                    onRemove = { viewModel.removeExpense(it) },
                    drafts = uiState.expenses,
                    onNext = { viewModel.goToSummaryStep() }
                )

                NewAccountStep.SUMMARY -> SummaryStep(
                    uiState = uiState,
                    onAccept = { viewModel.finishQuickSetup(onNavigateUp) }
                )
            }
        }
    }
}

@Composable
private fun DetailsStep(
    uiState: NewAccountUiState,
    viewModel: NewAccountViewModel,
    onNavigateUp: () -> Unit,
) {
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
        Text(text = uiState.errorMessage, color = Color.Red)
    }
    Button(
        onClick = { viewModel.goToQuickSetup() },
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSaving
    ) {
        Text(stringResource(R.string.quick_setup))
    }
    OutlinedButton(
        onClick = { viewModel.saveAccount(onNavigateUp) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSaving
    ) {
        Text(stringResource(R.string.skip_setup))
    }
}

@Composable
private fun RecurringStep(
    isIncome: Boolean,
    uiState: NewAccountUiState,
    viewModel: NewAccountViewModel,
    onNewCategory: () -> Unit,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    drafts: List<DraftTransaction>,
    onNext: () -> Unit,
) {
    Text(
        text = stringResource(
            if (isIncome) R.string.recurring_income_title else R.string.recurring_expense_title
        ),
        fontWeight = FontWeight.Bold
    )
    drafts.forEach { draft ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${draft.categoryName} · ${draft.amount} · ${draft.frequency.label}")
            IconButton(onClick = { onRemove(draft.localId) }) {
                Delete()
            }
        }
    }
    HorizontalDivider()
    OutlinedTextField(
        value = uiState.draftAmountInput,
        onValueChange = { viewModel.setDraftAmountInput(it) },
        label = { Text(text = stringResource(R.string.amount)) },
        isError = !uiState.isDraftAmountValid,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    CategoriesSearch(
        categories = uiState.categories,
        onItemSelect = { viewModel.setDraftCategoryId(it.id) },
        onAddCategory = onNewCategory
    )
    OutlinedTextField(
        value = uiState.draftDescriptionInput,
        onValueChange = { viewModel.setDraftDescriptionInput(it) },
        label = { Text(text = stringResource(R.string.description_optional)) },
        singleLine = false,
        modifier = Modifier.fillMaxWidth()
    )
    DateField(
        label = stringResource(R.string.date),
        dateInput = uiState.draftDateInput,
        onDateSelected = { viewModel.setDraftDateInput(it) }
    )
    FrequencySelector(
        frequency = uiState.draftFrequency,
        onFrequencySelected = { if (it != null) viewModel.setDraftFrequency(it) },
        allowOneTime = false
    )
    if (uiState.errorMessage != null) {
        Text(text = uiState.errorMessage, color = Color.Red)
    }
    OutlinedButton(
        onClick = onAdd,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.add))
    }
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.next))
    }
}

@Composable
private fun SummaryStep(
    uiState: NewAccountUiState,
    onAccept: () -> Unit,
) {
    Text(text = stringResource(R.string.summary_title), fontWeight = FontWeight.Bold)
    Text(text = "${uiState.nameInput} · ${uiState.startingBalanceInput.ifBlank { "0" }}")
    Text(text = stringResource(R.string.recurring_income_title), fontWeight = FontWeight.Bold)
    if (uiState.incomes.isEmpty()) {
        Text(text = "-")
    } else {
        uiState.incomes.forEach { draft ->
            Text(text = "${draft.categoryName} · ${draft.amount} · ${draft.frequency.label}")
        }
    }
    Text(text = stringResource(R.string.recurring_expense_title), fontWeight = FontWeight.Bold)
    if (uiState.expenses.isEmpty()) {
        Text(text = "-")
    } else {
        uiState.expenses.forEach { draft ->
            Text(text = "${draft.categoryName} · ${draft.amount} · ${draft.frequency.label}")
        }
    }
    if (uiState.errorMessage != null) {
        Text(text = uiState.errorMessage, color = Color.Red)
    }
    Button(
        onClick = onAccept,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSaving
    ) {
        Text(stringResource(R.string.accept_and_create_account))
    }
}
