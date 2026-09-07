package com.juanitos.ui.routes.money.goal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.lib.GoalType
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object SavingsGoalDestination : NavigationDestination {
    override val route = Routes.SavingsGoal
    override val titleRes = R.string.savings_goal
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalScreen(
    onNavigateUp: () -> Unit,
    viewModel: SavingsGoalViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.setNotificationsEnabled(granted) }

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(SavingsGoalDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
        )
    }) { innerPadding ->
        FormColumn(innerPadding) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                GoalType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = GoalType.entries.size,
                        ),
                        selected = type == uiState.goalType,
                        onClick = { viewModel.setGoalType(type) },
                    ) {
                        Text(type.label)
                    }
                }
            }

            when (uiState.goalType) {
                GoalType.FIXED -> OutlinedTextField(
                    value = uiState.fixedAmountInput,
                    onValueChange = { viewModel.setFixedAmountInput(it) },
                    label = { Text(text = stringResource(R.string.savings_goal_fixed_amount_label)) },
                    isError = !uiState.isFixedAmountValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

                GoalType.PERCENTAGE -> OutlinedTextField(
                    value = uiState.percentageInput,
                    onValueChange = { viewModel.setPercentageInput(it) },
                    label = { Text(text = stringResource(R.string.savings_goal_percentage_label)) },
                    isError = !uiState.isPercentageValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.savings_goal_notifications_label))
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.setNotificationsEnabled(enabled)
                        }
                    },
                )
            }

            if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage, color = Color.Red)
            }

            Button(
                onClick = { viewModel.saveGoal(onNavigateUp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
