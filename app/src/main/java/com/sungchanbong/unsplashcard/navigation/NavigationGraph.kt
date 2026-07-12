package com.sungchanbong.unsplashcard.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sungchanbong.feature.LikePhotoRoute
import com.sungchanbong.feature.MainListRoute
import com.sungchanbong.feature.PhotoDetailRoute
import com.sungchanbong.feature.detail.DetailScreen
import com.sungchanbong.feature.like.LikeScreen
import com.sungchanbong.feature.main.MainScreen

@Composable
fun NavigationGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MainListRoute) {
        composable<MainListRoute> {
            MainScreen(
                onNavigateToDetail = {
                    navController.navigateOnce(PhotoDetailRoute(photoId = it))
                }
            )
        }
        composable<PhotoDetailRoute> {
            DetailScreen(
                onNavigateBack = {}
            )
        }
        composable<LikePhotoRoute> {
            LikeScreen()
        }
    }
}