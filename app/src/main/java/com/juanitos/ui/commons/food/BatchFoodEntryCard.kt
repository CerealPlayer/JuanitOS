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
import com.juanitos.data.food.entities.relations.BatchFoodWithIngredientDetails

data class BatchFoodEntry(
    val batchFood: BatchFoodWithIngredientDetails,
    val qt: Int
)

@Composable
fun BatchFoodEntryCard(
    batchFoodEntry: BatchFoodEntry
) {
    val qtInt = batchFoodEntry.qt
    val calories = batchFoodEntry.batchFood.ingredients.sumOf {
        it.caloriesPer100 * it.grams / 100
    }
    val proteins = batchFoodEntry.batchFood.ingredients.sumOf {
        it.proteinsPer100 * it.grams / 100
    }
    val totalGramsInt = batchFoodEntry.batchFood.totalGrams
    val gramsRatio = qtInt / totalGramsInt.toFloat()

    val totalCalories = (gramsRatio * calories).toInt()
    val totalProteins = gramsRatio * proteins

    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
                Text(
                    text = batchFoodEntry.batchFood.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = stringResource(R.string.grams_ratio, qtInt, totalGramsInt))
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
