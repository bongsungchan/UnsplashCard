package com.sungchanbong.data.mapper

import com.sungchanbong.domain.models.PhotoError
import retrofit2.HttpException
import java.io.IOException
import kotlin.text.toIntOrNull

fun IOException.toPhotoError(): PhotoError = PhotoError.Network

fun HttpException.toPhotoError(): PhotoError = when (code()) {
    HTTP_UNAUTHORIZED -> PhotoError.Unauthorized
    HTTP_FORBIDDEN -> forbiddenReason()
    HTTP_NOT_FOUND -> PhotoError.NotFound
    else -> PhotoError.Unexpected(this)
}

private fun HttpException.forbiddenReason(): PhotoError {
    val remaining = response()?.headers()?.get(HEADER_RATELIMIT_REMAINING)?.toIntOrNull()
    return if (remaining == 0) PhotoError.RateLimited else PhotoError.Unauthorized
}

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private const val HEADER_RATELIMIT_REMAINING = "X-Ratelimit-Remaining"
