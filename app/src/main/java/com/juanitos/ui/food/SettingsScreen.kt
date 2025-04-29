package com.juanitos.ui.food

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import kotlinx.coroutines.launch

object FoodSettingsDestination : NavigationDestination {
    override val route = Routes.FoodSettings
    override val titleRes = R.string.settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: FoodSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.settingsUiState
    val coroutineScope = rememberCoroutineScope()
    val initCalLimit = viewModel.initialCalorieLimit.collectAsState().value
    val initProtLimit = viewModel.initialProteinLimit.collectAsState().value

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            navigateUp = onNavigateUp,
            title = stringResource(FoodSettingsDestination.titleRes),
            canNavigateBack = true
        )
    }) { padding ->
        FormColumn(padding) {
            OutlinedTextField(
                onValueChange = { viewModel.setCalorieLimit(it) },
                value = if (uiState.isCalEdited) uiState.calorieLimit else initCalLimit,
                label = { Text(stringResource(R.string.cal_obj_input)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !uiState.isCalLimitValid,
            )
            OutlinedTextField(
                onValueChange = { viewModel.setProteinLimit(it) },
                value = if (uiState.isProtEdited) uiState.proteinLimit else initProtLimit,
                label = { Text(stringResource(R.string.prot_obj_input)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = true,
                isError = !uiState.isProtLimitValid
            )
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.saveSettings()
                        onNavigateUp()
                    }
                },
                enabled = uiState.isProtLimitValid && uiState.isCalLimitValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Preview
@Composable
fun FoodSettingsScreenPreview() {
    FoodSettingsScreen(onNavigateUp = {})
}