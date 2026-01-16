package com.juanitos.ui.icons

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.juanitos.R

@Composable
fun Add() {
    Icon(
        painter = painterResource(R.drawable.add),
        contentDescription = "Add Icon"
    )
}