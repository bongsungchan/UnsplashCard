package com.sungchanbong.domain.models

sealed class PhotoError : Exception() {
    data object Network : PhotoError() {
        private fun readResolve(): Any = Network
    }


    data object RateLimited : PhotoError() {
        private fun readResolve(): Any = RateLimited
    }

    data object Unauthorized : PhotoError() {
        private fun readResolve(): Any = Unauthorized
    }

    data object NotFound : PhotoError() {
        private fun readResolve(): Any = NotFound
    }

    data class Unexpected(
        override val cause: Throwable?,
    ) : PhotoError()
}
