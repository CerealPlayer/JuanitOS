package com.juanitos.ui.food

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object FoodDestination : NavigationDestination {
    override val route = Routes.Food
    override val titleRes = R.string.food
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(
    onNavigateUp: () -> Unit,
    onSettings: () -> Unit,
    onNewFood: () -> Unit,
    viewModel: FoodViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val calorieLimt = viewModel.calorieLimit.collectAsState()
    val proteinLimit = viewModel.proteinLimit.collectAsState()
    val foods = viewModel.todaysFoods.collectAsState()

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(FoodDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewFood) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.new_food)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.calories_left, calorieLimt.value),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.prot_left, proteinLimit.value),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview
@Composable
fun FoodScreenPreview() {
    FoodScreen(
        onNavigateUp = {},
        onSettings = {},
        onNewFood = {} // Parámetro de vista previa
    )
}
