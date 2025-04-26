package com.juanitos.ui.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import com.juanitos.R
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
    onNavigateUp: () -> Unit
) {
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
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                onValueChange = {},
                value = "0",
                label = { Text(stringResource(R.string.cal_obj_input)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = true
            )
            OutlinedTextField(
                onValueChange = {},
                value = "0",
                label = { Text(stringResource(R.string.prot_obj_input)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = true
            )
        }
    }
}

@Preview
@Composable
fun FoodSettingsScreenPreview() {
    FoodSettingsScreen(onNavigateUp = {})
}