package com.weatherworld.util

import com.sletmoe.bucket4k.SuspendingBucket
import io.github.bucket4j.BandwidthBuilder
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
