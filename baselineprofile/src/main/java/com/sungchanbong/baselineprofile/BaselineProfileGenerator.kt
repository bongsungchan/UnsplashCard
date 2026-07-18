package com.sungchanbong.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.sungchanbong.unsplashcard",
    ) {
        pressHome()
        startActivityAndWait()

        device.wait(Until.hasObject(By.scrollable(true)), GRID_APPEAR_TIMEOUT_MS)
        val grid = device.findObject(By.scrollable(true))
        if (grid != null) {
            grid.setGestureMargin(device.displayWidth / GESTURE_MARGIN_RATIO)
            repeat(FLING_COUNT) {
                grid.fling(Direction.DOWN)
                device.waitForIdle()
            }
        }
    }

    private companion object {
        const val GRID_APPEAR_TIMEOUT_MS = 5_000L

        const val GESTURE_MARGIN_RATIO = 5

        const val FLING_COUNT = 3
    }
}
