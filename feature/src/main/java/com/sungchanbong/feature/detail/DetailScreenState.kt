package com.sungchanbong.feature.detail

import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState
import com.sungchanbong.domain.models.PhotoDetail

data class DetailScreenState(
    val isLoading: Boolean = false,
    val detail: PhotoDetail? = null,
) : UIState

sealed interface DetailScreenIntent : UIIntent {

}

sealed interface DetailScreenEffect : UIEffect {

}