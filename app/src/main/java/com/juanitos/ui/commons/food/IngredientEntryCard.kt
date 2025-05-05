package com.juanitos.ui.commons.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.juanitos.R
import com.juanitos.data.food.entities.Ingredient

data class IngredientEntry(
    val ingredient: Ingredient, val qt: String
)

@Composable
fun IngredientEntryCard(
    ingredientEntry: IngredientEntry
) {
    val qtInt = ingredientEntry.qt.toIntOrNull() ?: 0
    val calories = ingredientEntry.ingredient.caloriesPer100.toIntOrNull() ?: 0
    val proteins = ingredientEntry.ingredient.proteinsPer100.toIntOrNull() ?: 0
    val totalCalories = (qtInt * calories) / 100
    val totalProteins = (qtInt * proteins) / 100
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.padding_medium)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = ingredientEntry.ingredient.name, style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(R.string.food_ingredient_qt, qtInt))
            }
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.food_ingredient_cals, totalCalories))
                Text(text = stringResource(R.string.food_ingredient_prot, totalProteins))
            }
        }
    }
}