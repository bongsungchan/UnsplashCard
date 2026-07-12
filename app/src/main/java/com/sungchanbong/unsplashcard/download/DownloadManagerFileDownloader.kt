package com.sungchanbong.unsplashcard.download

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.sungchanbong.domain.download.PhotoFileDownloader
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManagerFileDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
) : PhotoFileDownloader {

    override fun isPermissionRequired(): Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) return false
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) != PackageManager.PERMISSION_GRANTED
    }

    override fun download(url: String, fileName: String): Result<Unit> = runCatching {
        val downloadManager = checkNotNull(context.getSystemService<DownloadManager>()) {
            "DownloadManager 를 사용할 수 없는 기기입니다."
        }

        val request = DownloadManager.Request(url.toUri())
            .setTitle(fileName)
            .setMimeType(MIME_JPEG)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName)

            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        downloadManager.enqueue(request)
        Unit
    }

    private companion object {
        const val MIME_JPEG = "image/jpeg"
    }
}
