package com.sungchanbong.unsplashcard

import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ImageLoaderWiringTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var injectedImageLoader: ImageLoader

    @Before
    fun setUp() = hiltRule.inject()

    @Test
    fun UnsplashCardApp_이_Coil_싱글턴에_Hilt_로더를_등록한다() {
        assertTrue(
            "UnsplashCardApp 이 ImageLoaderFactory 를 구현하지 않으면 AsyncImage 는 Coil 기본 로더를 쓴다 " +
                    "→ 프리페처가 채운 디스크 캐시를 화면이 못 읽어 오프라인이 깨진다.",
            ImageLoaderFactory::class.java.isAssignableFrom(UnsplashCardApp::class.java),
        )
    }

    @Test
    fun 주입된_로더의_디스크_캐시가_오프라인을_감당할_크기다() {
        val diskCache = injectedImageLoader.diskCache
        assertTrue("디스크 캐시가 구성되지 않았습니다", diskCache != null)
        assertTrue(
            "디스크 캐시가 너무 작습니다: ${diskCache?.maxSize}",
            (diskCache?.maxSize ?: 0) >= MIN_DISK_CACHE_BYTES,
        )
    }

    private companion object {
        const val MIN_DISK_CACHE_BYTES = 128L * 1024 * 1024
    }
}
