package com.weatherworld.util

import com.sletmoe.bucket4k.SuspendingBucket
import io.github.bucket4j.BandwidthBuilder
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
