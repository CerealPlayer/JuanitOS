package com.juanitos.ui.commons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import com.juanitos.R

@Composable
fun IngredientQtDialog(
    name: String,
    onDismissRequest: () -> Unit,
    qt: String,
    onQtChange: (String) -> Unit,
    onSave: () -> Unit,
    isError: Boolean = false,
    customMessage: @Composable (() -> Unit) = {}
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card {
            Column(
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.padding_medium))
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = qt,
                    onValueChange = onQtChange,
                    label = { Text(stringResource(R.string.qt)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError,
                    singleLine = true,
                )
                customMessage()
                Button(
                    onClick = onSave, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.add))
                }
            }
        }
    }
}