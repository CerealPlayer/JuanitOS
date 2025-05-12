package com.juanitos.ui.routes.food.batch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.juanitos.R
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object BatchFoodsDestination : NavigationDestination {
    override val route = Routes.BatchFoods
    override val titleRes = R.string.batch_foods
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchFoodsScreen(
    onNavigateUp: () -> Unit,
    onNewBatchFood: () -> Unit,
) {
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(BatchFoodsDestination.titleRes, "some"),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                actions = {
                    IconButton(onNewBatchFood) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add),
                        )
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
            Text("batch foods")
        }
    }
}