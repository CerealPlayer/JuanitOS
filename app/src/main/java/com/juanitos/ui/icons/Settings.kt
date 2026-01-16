package com.juanitos.ui.icons

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.juanitos.R

@Composable
fun Settings() {
    Icon(
        painter = painterResource(R.drawable.settings),
        contentDescription = "Settings"
    )
}