package com.juanitos.ui.commons.categories_search

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.juanitos.data.money.entities.Category
import com.juanitos.ui.commons.search.Search
import com.juanitos.ui.commons.search.SearchResult

@Composable
fun CategeoriesSearch(
    categories: List<Category>,
    onItemSelect: (Category) -> Unit
) {
    var query: String by remember { mutableStateOf("") }
    var expanded: Boolean by remember { mutableStateOf(false) }
    Search(
        query = query,
        expanded = expanded,
        onQueryChange = {
            query = it
            expanded = it.isNotEmpty()
        },
        onExpandedChange = { expanded = it },
        onSearch = { expanded = false },
        searchResults = categories.filter { it.name.contains(query, ignoreCase = true) }.map {
            SearchResult(
                id = it.id.toString(),
                label = { Text(it.name) },
                onItemSelect = { onItemSelect(it) }
            )
        }
    )
}