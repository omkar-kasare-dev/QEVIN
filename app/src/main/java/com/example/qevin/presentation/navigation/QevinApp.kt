package com.example.qevin.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun QevinApp() {

    val navController = rememberNavController()

    QevinNavGraph(
        navController = navController
    )
}