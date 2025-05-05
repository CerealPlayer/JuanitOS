package com.juanitos.ui.commons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.juanitos.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    query: String,
    expanded: Boolean,
    onQueryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<String>,
    onItemSelect: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small))
    ) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    placeholder = { Text(stringResource(R.string.search_ingredient)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                        )
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = onExpandedChange,
        ) {
            LazyColumn {
                items(searchResults) { item ->
                    ListItem(headlineContent = { Text(item) },
                        modifier = Modifier
                            .clickable {
                                onQueryChange(item)
                                onItemSelect(item)
                                onExpandedChange(false)
                            }
                            .fillMaxWidth())

                }
            }
        }
    }
}