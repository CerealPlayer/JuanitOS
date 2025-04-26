package com.juanitos.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.juanitos.R
import com.juanitos.ui.food.FoodDestination
import com.juanitos.ui.money.MoneyDestination
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.Routes

object HomeDestination : NavigationDestination {
    override val route = Routes.Home
    override val titleRes = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateTo: (Routes) -> Unit
) {
    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(HomeDestination.titleRes),
            canNavigateBack = false,
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubAppCard(textId = R.string.food, iconId = R.drawable.subapp_food, onClick = { onNavigateTo(FoodDestination.route) })
                SubAppCard(textId = R.string.money, iconId = R.drawable.subapp_money, onClick = { onNavigateTo(MoneyDestination.route) })
            }
        }
    }
}

@Composable
fun SubAppCard(textId: Int, iconId: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .size(128.dp)
            .padding(dimensionResource(id = R.dimen.padding_small))
            .clickable(
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = stringResource(textId),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview
@Composable
fun HomePreview() {
    HomeScreen(onNavigateTo = {})
}