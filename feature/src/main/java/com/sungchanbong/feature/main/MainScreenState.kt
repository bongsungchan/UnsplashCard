package com.sungchanbong.feature.main

import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState

data class MainScreenState(
    val isLoading: Boolean = false
) : UIState

sealed interface MainScreenIntent : UIIntent {
    data class PhotoClicked(val photoId: String) : MainScreenIntent
}

sealed interface MainScreenEffect : UIEffect {
    data class NavigateToDetail(val photoId: String) : MainScreenEffect
}