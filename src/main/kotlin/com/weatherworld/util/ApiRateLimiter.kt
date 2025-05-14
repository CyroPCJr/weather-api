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
            val tokenPerRefill = 1000L
            val refillDuration: Duration? = Duration.ofDays(1)

            addLimit {
                BandwidthBuilder
                    .builder()
                    .capacity(
                        tokenPerRefill,
                    ).refillGreedy(tokenPerRefill, refillDuration)
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
