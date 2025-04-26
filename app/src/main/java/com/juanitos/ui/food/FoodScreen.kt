package com.juanitos.ui.food

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.juanitos.R
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
    onSettings: () -> Unit
) {
    Scaffold(topBar = {
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
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.calories_left, 0),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Preview
@Composable
fun FoodScreenPreview() {
    FoodScreen(onNavigateUp = {}, onSettings = {})
}