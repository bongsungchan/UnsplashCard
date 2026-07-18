package com.sungchanbong.domain.download

interface PhotoFileDownloader {
    fun isPermissionRequired(): Boolean
    suspend fun download(
        url: String,
        fileName: String
    ): Result<Unit>
}