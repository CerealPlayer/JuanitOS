package com.juanitos.ui.routes.habit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.juanitos.R
import com.juanitos.data.habit.entities.relations.HabitWithEntries

@Composable
fun HabitCard(
    habitWithEntries: HabitWithEntries,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(R.dimen.padding_small))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Text(
                text = habitWithEntries.habit.name,
                style = MaterialTheme.typography.titleMedium,
            )
            if (!habitWithEntries.habit.description.isNullOrBlank()) {
                Text(
                    text = habitWithEntries.habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
