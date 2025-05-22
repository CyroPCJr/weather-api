package com.weatherworld.exception

import feign.FeignException

object FeignErrorHandler {
    inline fun <T> handle(
        throwable: Throwable,
        onNotFound: () -> T,
    ): T =
        when (throwable) {
            is FeignException.NotFound -> onNotFound()
            is FeignException -> throw ExternalApiException("Feign error: ${throwable.message}")
            else -> throw ExternalApiException("Unknow error: ${throwable.message}")
        }
}
