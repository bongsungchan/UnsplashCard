package com.sungchanbong.domain.image

interface ImagePrefetcher {
    fun prefetch(urls: List<String>)
}