package com.sungchanbong.feature.testutil

import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

fun photo(id: String = "p1", isLike: Boolean = false) = Photo(
    id = id,
    description = "desc",
    thumbUrl = "thumb",
    fullUrl = "full",
    width = 400,
    height = 600,
    authorName = "Alex",
    authorUsername = "alex",
    authorProfileImageUrl = null,
    likes = 7,
    isLike = isLike,
)

fun photoDetail(
    id: String = "p1",
    isLike: Boolean = false,
    isStale: Boolean = false,
) = PhotoDetail(
    photo = photo(id, isLike),
    views = 120,
    downloads = 30,
    location = "Seoul",
    exifModel = "Canon EOS R5",
    tags = listOf("nature"),
    isStale = isStale,
)
