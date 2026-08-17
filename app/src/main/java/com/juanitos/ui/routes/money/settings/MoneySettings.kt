package com.juanitos.ui.routes.money.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.lib.formatDbDatetimeToShortDate
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = uiState.cycleSetupMode == CycleSetupMode.MANUAL,
                            onClick = { viewModel.setCycleSetupMode(CycleSetupMode.MANUAL) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.cycleSetupMode == CycleSetupMode.MANUAL,
                        onClick = { viewModel.setCycleSetupMode(CycleSetupMode.MANUAL) }
                    )
                    Text(text = stringResource(R.string.cycle_setup_mode_manual))
                }
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = uiState.cycleSetupMode == CycleSetupMode.SCHEDULED,
                            onClick = { viewModel.setCycleSetupMode(CycleSetupMode.SCHEDULED) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.cycleSetupMode == CycleSetupMode.SCHEDULED,
                        onClick = { viewModel.setCycleSetupMode(CycleSetupMode.SCHEDULED) }
                    )
                    Text(text = stringResource(R.string.cycle_setup_mode_scheduled))
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small))
            )

            if (uiState.cycleSetupMode == CycleSetupMode.SCHEDULED) {
                Text(text = stringResource(R.string.income_schedule))
                OutlinedTextField(
                    value = uiState.scheduleDayInput,
                    onValueChange = { viewModel.setScheduleDayInput(it) },
                    singleLine = true,
                    isError = !uiState.isScheduleDayValid,
                    label = { Text(stringResource(R.string.income_schedule_day_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = uiState.scheduleAmountInput,
                    onValueChange = { viewModel.setScheduleAmountInput(it) },
                    singleLine = true,
                    isError = !uiState.isScheduleAmountValid,
                    label = { Text(stringResource(R.string.income_input_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.scheduleAdjustForWeekends,
                            onClick = {
                                viewModel.setScheduleAdjustForWeekends(!uiState.scheduleAdjustForWeekends)
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.scheduleAdjustForWeekends,
                        onCheckedChange = { viewModel.setScheduleAdjustForWeekends(it) }
                    )
                    Text(text = stringResource(R.string.income_schedule_adjust_for_weekends))
                }
                Button(
                    onClick = { viewModel.saveSchedule() },
                    enabled = uiState.scheduleDayInput.isNotBlank() && uiState.scheduleAmountInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.save_schedule))
                }
                val previewDay = uiState.scheduleDayInput.takeIf { uiState.isScheduleDayValid }
                    ?.toIntOrNull()
                val previewAmount =
                    uiState.scheduleAmountInput.takeIf { uiState.isScheduleAmountValid }
                if (previewDay != null && !previewAmount.isNullOrBlank()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                    ) {
                        Text(
                            text = stringResource(
                                R.string.income_schedule_active_message,
                                previewDay,
                                previewAmount
                            )
                        )
                        if (uiState.scheduleAdjustForWeekends) {
                            Text(text = stringResource(R.string.income_schedule_weekend_note))
                        }
                    }
                }

                if (uiState.schedule != null && uiState.currentCycle != null) {
                    Text(
                        text = stringResource(
                            R.string.cycle_summary,
                            formatDbDatetimeToShortDate(uiState.currentCycle?.startDate),
                            uiState.currentCycle?.totalIncome.toString()
                        )
                    )
                    Button(
                        onClick = { viewModel.closeCycleAndStartNewNow() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.close_cycle_start_new_now))
                    }
                }
            } else {
                Text(text = stringResource(R.string.current_cycle))
                if (uiState.currentCycle == null) {
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
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Button(
                            onClick = { viewModel.createNewCycle(onNavigateUp) },
                            enabled = uiState.isIncomeValid && uiState.incomeInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.create_cycle))
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = uiState.editIncomeInput,
                        onValueChange = { viewModel.setEditIncomeInput(it) },
                        singleLine = true,
                        isError = !uiState.isEditIncomeValid,
                        label = { Text(stringResource(R.string.income_input_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = uiState.editStartDateInput,
                        onValueChange = { viewModel.setEditStartDateInput(it) },
                        singleLine = true,
                        isError = !uiState.isEditStartDateValid,
                        label = { Text(stringResource(R.string.cycle_start_date_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(text = "Fin: ${uiState.currentCycle?.endDate ?: "(abierto)"}")
                    Button(
                        onClick = { viewModel.saveCycleEdits() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.save_changes))
                    }
                    Button(
                        onClick = { viewModel.endCurrentCycle() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.end_cycle))
                    }
                }
            }
            if (!uiState.errorMessage.isNullOrEmpty()) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = Color.Red
                )
            }
        }
    }
}
