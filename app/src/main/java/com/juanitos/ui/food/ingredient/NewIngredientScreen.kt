package com.juanitos.ui.food.ingredient

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object NewIngredientDestination : NavigationDestination {
    override val route = Routes.NewIngredient
    override val titleRes = R.string.new_ingredient
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewIngredientScreen(
    onNavigateUp: () -> Unit,
    viewModel: NewIngredientViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val name = viewModel.uiState.name
    val calories = viewModel.uiState.calories
    val protein = viewModel.uiState.protein

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewIngredientDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
        )
    }) { innerPadding ->
        FormColumn(innerPadding) {
            OutlinedTextField(
                value = name.value,
                onValueChange = {
                    viewModel.updateName(it)
                },
                label = { Text(stringResource(R.string.ingredient_name)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = name.touched && !name.isValid,
            )
            OutlinedTextField(
                value = calories.value,
                onValueChange = {
                    viewModel.updateCalories(it)
                },
                label = { Text(stringResource(R.string.ingredient_calories)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = calories.touched && !calories.isValid,
            )
            OutlinedTextField(
                value = protein.value,
                onValueChange = {
                    viewModel.updateProtein(it)
                },
                label = { Text(stringResource(R.string.ingredient_protein)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = protein.touched && !protein.isValid,
            )
            Button(
                onClick = {
                    viewModel.saveIngredient(onNavigateUp)
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
            Button(
                onClick = onNavigateUp,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}