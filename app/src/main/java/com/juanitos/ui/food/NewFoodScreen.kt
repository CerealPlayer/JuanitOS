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

object NewFoodDestination : NavigationDestination {
    override val route = Routes.NewFood
    override val titleRes = R.string.new_food
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFoodScreen(
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(NewFoodDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        }
    ) { innerPadding ->
        Text("new food", modifier = Modifier.padding(innerPadding))
    }
}