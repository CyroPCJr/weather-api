package com.weatherworld.util

import com.sletmoe.bucket4k.SuspendingBucket
import com.weatherworld.exception.GlobalExceptionHandler
import io.github.bucket4j.BandwidthBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class ApiRateLimiter {
    companion object {
        const val TOKEN_PER_REFILL_IN_MINUTES = 60L
        const val TOKEN_PER_REFILL_PER_MONTH = 1_000_000L
    }

    private val tokenBucket =
        SuspendingBucket.build {
            val refillDurationMinutes: Duration? = Duration.ofMinutes(1)
            val refillDurationMonth: Duration? = Duration.ofDays(30)

            addLimit {
                BandwidthBuilder
                    .builder()
                    .capacity(
                        TOKEN_PER_REFILL_IN_MINUTES,
                    ).refillGreedy(TOKEN_PER_REFILL_IN_MINUTES, refillDurationMinutes)
            }
            addLimit {
                BandwidthBuilder
                    .builder()
                    .capacity(TOKEN_PER_REFILL_PER_MONTH)
                    .refillGreedy(TOKEN_PER_REFILL_PER_MONTH, refillDurationMonth)
            }
        }

    fun tryConsume(): Boolean = tokenBucket.tryConsume(1)
}

fun ApiRateLimiter.withRateLimit(block: () -> ResponseEntity<Any>): ResponseEntity<Any> =
    if (this.tryConsume()) {
        block()
    } else {
        val error = GlobalExceptionHandler.ErrorResponse("429", "Rate limit exceeded. Try again later.")
        ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error)
    }

suspend fun ApiRateLimiter.withRateLimitCo(block: suspend () -> ResponseEntity<Any>): ResponseEntity<Any> =
    if (this.tryConsume()) {
        block()
    } else {
        val error = GlobalExceptionHandler.ErrorResponse("429", "Rate limit exceeded. Try again later.")
        ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error)
    }
