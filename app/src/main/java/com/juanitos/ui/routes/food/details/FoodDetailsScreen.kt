package com.juanitos.ui.routes.food.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object FoodDetailsDestination : NavigationDestination {
    override val route: Routes = Routes.FoodDetails
    override val titleRes = R.string.food_detail_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailsScreen(
    onNavigateUp: () -> Unit,
    viewModel: FoodDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val foodId = viewModel.foodId
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(FoodDetailsDestination.titleRes, foodId),
                navigateUp = onNavigateUp,
                canNavigateBack = true
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("Food Details Screen")
        }
    }
}