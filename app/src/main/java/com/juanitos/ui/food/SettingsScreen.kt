package com.juanitos.ui.food

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

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
    Scaffold(topBar = {
        JuanitOSTopAppBar(
            navigateUp = onNavigateUp,
            title = stringResource(FoodSettingsDestination.titleRes),
            canNavigateBack = true
        )
    }) { padding ->
        Column(
            modifier = Modifier.padding(
                top = padding.calculateTopPadding() + 24.dp,
                bottom = padding.calculateBottomPadding(),
                start = dimensionResource(R.dimen.padding_medium),
                end = dimensionResource(R.dimen.padding_medium)
            ), verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                onValueChange = { viewModel.setCalorieLimit(it) },
                value = uiState.calorieLimit,
                label = { Text(stringResource(R.string.cal_obj_input)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !uiState.isCalLimitValid,
            )
            OutlinedTextField(
                onValueChange = { viewModel.setProteinLimit(it) },
                value = uiState.proteinLimit,
                label = { Text(stringResource(R.string.prot_obj_input)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = true,
                isError = !uiState.isProtLimitValid
            )
            Button(
                onClick = {},
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