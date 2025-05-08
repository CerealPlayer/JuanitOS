package com.juanitos.ui.routes.food.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
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
    val food = viewModel.food.collectAsState().value
        ?: return

    val foodDetails = food.toFormattedFoodDetails()
    Scaffold(topBar = {
        JuanitOSTopAppBar(title = stringResource(FoodDetailsDestination.titleRes, foodDetails.id),
            navigateUp = onNavigateUp,
            canNavigateBack = true,
            actions = {
                IconButton(onClick = { viewModel.deleteFood(onNavigateUp) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete),
                    )
                }
            })
    }) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.padding_medium),
                end = dimensionResource(R.dimen.padding_medium),
                bottom = paddingValues.calculateBottomPadding(),
                top = paddingValues.calculateTopPadding()
            )
        ) {
            Text(foodDetails.name)
            Text(stringResource(R.string.cal_summary, foodDetails.totalCalories))
            Text(stringResource(R.string.prot_summary, foodDetails.totalProteins))
        }
    }
}