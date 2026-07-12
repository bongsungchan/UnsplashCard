package com.sungchanbong.unsplashcard.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

private val NavController.isCurrentScreenResumed: Boolean
    get() = currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

fun NavController.navigateOnce(route: Any) {
    if (isCurrentScreenResumed) navigate(route)
}

fun NavController.popBackStackOnce() {
    if (isCurrentScreenResumed) popBackStack()
}
