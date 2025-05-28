package com.weatherworld.exception

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.Instant

@ControllerAdvice
class GlobalExceptionHandler(
    @Value("\${spring.profiles.active:prod}")
    private val activeProfile: String,
) {
    data class ErrorResponse(
        val timestamp: String = Instant.now().toString(),
        val status: Int,
        val error: String,
        val message: String,
        val path: String,
    )

    @ExceptionHandler(CityNotFoundException::class)
    fun handleCityNotFound(ex: CityNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to ex.message.orEmpty()))

    @ExceptionHandler(RateLimitExceededException::class)
    fun handleRateLimitExceeded(
        ex: RateLimitExceededException,
        request: ServerHttpRequest,
    ): ResponseEntity<Any> = buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex, request)

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: ServerHttpRequest,
    ): ResponseEntity<Any> = buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request)

    private fun buildResponse(
        status: HttpStatus,
        ex: Exception,
        request: ServerHttpRequest,
    ): ResponseEntity<Any> =
        if (activeProfile == "prod") {
            ResponseEntity
                .status(status)
                .body(
                    mapOf(
                        "cod" to status.value().toString(),
                        "message" to
                            when (ex) {
                                is CityNotFoundException -> "City not found"
                                is RateLimitExceededException -> "Too many requests"
                                else -> "Unexpected server error"
                            },
                    ),
                )
        } else {
            val errorResponse =
                ErrorResponse(
                    status = status.value(),
                    error = ex.javaClass.simpleName,
                    message = ex.message ?: "No message available",
                    path = request.uri.path,
                )
            ResponseEntity.status(status).body(errorResponse)
        }
}
