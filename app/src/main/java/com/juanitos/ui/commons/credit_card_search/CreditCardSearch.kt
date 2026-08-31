package com.juanitos.ui.commons.credit_card_search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.juanitos.R
import com.juanitos.data.money.entities.CreditCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardSearch(
    creditCards: List<CreditCard>,
    onItemSelect: (CreditCard) -> Unit,
    onAddCreditCard: () -> Unit,
    initialQuery: String = ""
) {
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }
    var expanded by remember { mutableStateOf(false) }

    val filteredCreditCards by remember(creditCards, query) {
        derivedStateOf {
            if (query.isBlank()) creditCards
            else creditCards.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
                expanded = newQuery.isNotBlank() && filteredCreditCards.isNotEmpty()
            },
            label = { Text(stringResource(R.string.credit_card_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_credit_card)) },
                onClick = onAddCreditCard,
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            filteredCreditCards.forEach { creditCard ->
                DropdownMenuItem(
                    text = { Text(creditCard.name) },
                    onClick = {
                        query = creditCard.name
                        onItemSelect(creditCard)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
