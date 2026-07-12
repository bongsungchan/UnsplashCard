package com.sungchanbong.data.util

import javax.inject.Inject
import javax.inject.Singleton

fun interface Clock {
    fun now(): Long
}

@Singleton
class SystemClock @Inject constructor() : Clock {
    override fun now(): Long = System.currentTimeMillis()
}
