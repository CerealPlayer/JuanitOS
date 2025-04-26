package com.juanitos.ui.food

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        Text("settings", modifier = Modifier.padding(padding))
    }
}