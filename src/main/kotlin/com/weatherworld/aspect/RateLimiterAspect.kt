package com.weatherworld.aspect

import com.weatherworld.exception.RateLimitExceededException
import com.weatherworld.util.ApiRateLimiter
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class RateLimiterAspect(
    private val rateLimiter: ApiRateLimiter,
) {
    @Around("@annotation(RateLimited)")
    fun enforceRateLimit(joinPoint: ProceedingJoinPoint): Any {
        if (!rateLimiter.tryConsume()) {
            throw RateLimitExceededException("Rate limit exceeded. Try again later.")
        }

        return joinPoint.proceed()
    }
}
