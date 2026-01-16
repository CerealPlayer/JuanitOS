package com.juanitos.ui.routes.food.ingredients.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.juanitos.ui.icons.Delete
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object IngredientDetailsDestination : NavigationDestination {
    override val route = Routes.IngredientDetails
    override val titleRes = R.string.ingredient_detail_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailsScreen(
    navigateUp: () -> Unit,
    viewModel: IngredientDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val ingredient = viewModel.ingredient.collectAsState().value ?: return
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(IngredientDetailsDestination.titleRes, ingredient.name),
                canNavigateBack = true,
                navigateUp = navigateUp,
                actions = {
                    IconButton(onClick = { viewModel.deleteIngredient(navigateUp) }) {
                        Delete()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
            )
        ) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
            )
            Text(
                text = stringResource(R.string.cal_summary, ingredient.caloriesPer100),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
            )
            Text(
                text = stringResource(R.string.prot_summary, ingredient.proteinsPer100),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
            )
        }
    }
}