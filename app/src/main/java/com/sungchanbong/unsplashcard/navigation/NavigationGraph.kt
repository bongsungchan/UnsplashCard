package com.sungchanbong.unsplashcard.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sungchanbong.feature.detail.DetailScreen
import com.sungchanbong.feature.like.LikeScreen
import com.sungchanbong.feature.main.MainScreen

@Composable
fun NavigationGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScreen()
        }
        composable(Routes.DETAIL) {
            DetailScreen()
        }
        composable(Routes.LIKE) {
            LikeScreen()
        }
    }
}