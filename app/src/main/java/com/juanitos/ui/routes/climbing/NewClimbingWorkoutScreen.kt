package com.juanitos.ui.routes.climbing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.juanitos.R
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object NewClimbingWorkoutDestination : NavigationDestination {
    override val route = Routes.NewClimbingWorkout
    override val titleRes = R.string.new_climbing_workout
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewClimbingWorkoutScreen(
    onNavigateUp: () -> Unit,
    onNewBoulder: () -> Unit,
) {
    var notes by rememberSaveable { mutableStateOf("") }
    var showBoulderDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(NewClimbingWorkoutDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(dimensionResource(R.dimen.padding_small))
        ) {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.initial_notes)) },
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_small)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.add_boulder),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showBoulderDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.add),
                            contentDescription = stringResource(R.string.add_boulder)
                        )
                    }
                }
            }
        }
    }

    if (showBoulderDialog) {
        AlertDialog(
            onDismissRequest = { showBoulderDialog = false },
            title = { Text(text = stringResource(R.string.select_boulder)) },
            text = {
                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBoulderDialog = false
                                onNewBoulder()
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.padding_medium)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.add),
                                contentDescription = stringResource(R.string.new_boulder),
                                modifier = Modifier.size(dimensionResource(R.dimen.padding_medium) * 2)
                            )
                            Text(text = stringResource(R.string.new_boulder))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBoulderDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}
