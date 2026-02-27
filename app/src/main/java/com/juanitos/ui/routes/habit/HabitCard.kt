package com.juanitos.ui.routes.habit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.juanitos.R
import com.juanitos.data.habit.entities.relations.HabitWithEntries
import com.juanitos.lib.formatDbDatetimeToShortDate

@Composable
fun HabitCard(
    habitWithEntries: HabitWithEntries,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(bottom = dimensionResource(R.dimen.padding_small))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        ) {
            val createdAt = formatDbDatetimeToShortDate(habitWithEntries.habit.createdAt)
                .ifBlank { habitWithEntries.habit.createdAt.orEmpty() }
                .ifBlank { stringResource(R.string.habit_unknown_date) }
            val completedAt = formatDbDatetimeToShortDate(habitWithEntries.habit.completedAt)
                .ifBlank { habitWithEntries.habit.completedAt.orEmpty() }
            Text(
                text = habitWithEntries.habit.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (completedAt.isNotBlank()) {
                    stringResource(R.string.habit_lifecycle_from_to, createdAt, completedAt)
                } else {
                    stringResource(R.string.habit_lifecycle_from, createdAt)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
