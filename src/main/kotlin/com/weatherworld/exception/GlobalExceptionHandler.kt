package com.weatherworld.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.Instant

@ControllerAdvice
class GlobalExceptionHandler(
    @Value("\${spring.profiles.active:prod}")
    private val activeProfile: String,
) {
    data class ErrorResponse(
        val cod: String,
        val message: String,
    )

    @ExceptionHandler(CityNotFoundException::class)
    fun handleCityNotFound(
        ex: CityNotFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<Any> =
        if (activeProfile == "prod") {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("cod" to "404", "message" to "city not found"))
        } else {
            val body =
                mapOf(
                    "timestamp" to Instant.now().toString(),
                    "status" to 404,
                    "error" to ex.javaClass.simpleName,
                    "message" to ex.message,
                    "path" to request.requestURI,
                )
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
        }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<Any> =
        if (activeProfile == "prod") {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("cod" to "500", "message" to "unexpected server error"))
        } else {
            val body =
                mapOf(
                    "timestamp" to Instant.now().toString(),
                    "status" to 500,
                    "error" to ex.javaClass.simpleName,
                    "message" to ex.message,
                    "path" to request.requestURI,
                )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
        }

    @ExceptionHandler(RateLimitExceededException::class)
    fun handleRateLimitExceeded(ex: RateLimitExceededException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ErrorResponse("429", ex.message ?: "Too many requests"))
}
