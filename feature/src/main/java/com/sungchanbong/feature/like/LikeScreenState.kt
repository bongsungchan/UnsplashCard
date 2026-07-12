package com.sungchanbong.feature.like

import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState

data class LikeScreenState(
    val isLoading: Boolean = false
) : UIState

sealed interface LikeScreenIntent : UIIntent {

}

sealed interface LikeScreenEffect : UIEffect {

}