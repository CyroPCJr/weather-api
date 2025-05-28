package com.weatherworld.exception

import org.springframework.web.reactive.function.client.WebClientResponseException

object HttpErrorHandler {
    inline fun <T> handle(
        throwable: Throwable,
        onNotFound: () -> T,
    ): T =
        when (throwable) {
            is WebClientResponseException.NotFound -> onNotFound()
            is WebClientResponseException -> throw ExternalApiException("API error: ${throwable.statusCode} - ${throwable.message}")
            else -> throw ExternalApiException("Unknown error: ${throwable.message}")
        }
}
