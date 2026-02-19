package com.juanitos.ui.commons.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.juanitos.R
import com.juanitos.ui.icons.Search

data class SearchResult(
    val id: String,
    val label: @Composable () -> Unit,
    val tag: @Composable () -> Unit = {},
    val onItemSelect: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    query: String,
    placeholder: String? = null,
    expanded: Boolean,
    onQueryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<SearchResult>,
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
                    placeholder = {
                        Text(placeholder ?: "")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Search()
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = onExpandedChange,
        ) {
            LazyColumn {
                items(searchResults) { item ->
                    ListItem(
                        headlineContent = { item.label() },
                        trailingContent = { item.tag() },
                        modifier = Modifier
                            .clickable {
                                onQueryChange(item.id)
                                item.onItemSelect()
                                onExpandedChange(false)
                            }
                            .fillMaxWidth())

                }
            }
        }
    }
}