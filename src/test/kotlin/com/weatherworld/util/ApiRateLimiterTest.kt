package com.weatherworld.util

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApiRateLimiterTest {
    @Test
    fun `should allow up to 1000 requests per day`() =
        runTest {
            val limiter = ApiRateLimiter()

            repeat(1000) {
                assertTrue(limiter.tryConsume(), "Should allow request $it")
            }

            assertFalse(limiter.tryConsume(), "Should deny request after limit is reached")
        }
}
