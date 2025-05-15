package com.weatherworld.util

import com.sletmoe.bucket4k.SuspendingBucket
import com.weatherworld.exception.GlobalExceptionHandler
import io.github.bucket4j.BandwidthBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.time.Duration

@Component
final class ApiRateLimiter {
    private val tokenBucket =
        SuspendingBucket.build {
            val tokenPerRefillMinutes = 60L
            val tokenPerRefillMonth = 1_000_000L
            val refillDurationMinutes: Duration? = Duration.ofMinutes(1)
            val refillDurationMonth: Duration? = Duration.ofDays(30)

            addLimit {
                BandwidthBuilder
                    .builder()
                    .capacity(
                        tokenPerRefillMinutes,
                    ).refillGreedy(tokenPerRefillMinutes, refillDurationMinutes)
            }
            addLimit {
                BandwidthBuilder
                    .builder()
                    .capacity(tokenPerRefillMonth)
                    .refillGreedy(tokenPerRefillMonth, refillDurationMonth)
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
