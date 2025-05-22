package com.weatherworld.component

import com.weatherworld.exception.RateLimitExceededException
import com.weatherworld.util.ApiRateLimiter
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Aspecto que aplica limitação de taxa (rate limiting) em métodos anotados com @RateLimited.
 */
@Aspect
@Component("customRateLimiterAspect")
class CustomRateLimiterAspect(
    private val rateLimiter: ApiRateLimiter,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Around("@annotation(RateLimited)")
    fun enforceRateLimit(joinPoint: ProceedingJoinPoint): Any {
        logger.info("Aspecto chamado para: ${joinPoint.signature}")
        if (!rateLimiter.tryConsume()) {
            logger.warn("Rate limit exceeded for method: ${joinPoint.signature}")
            throw RateLimitExceededException("Rate limit exceeded. Try again later.")
        }

        return joinPoint.proceed()
    }
}
