package com.weatherworld.component

import com.weatherworld.exception.RateLimitExceededException
import com.weatherworld.util.ApiRateLimiter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class RateLimitFilter(
    private val rateLimiter: ApiRateLimiter,
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void?> =
        if (!rateLimiter.tryConsume()) {
            exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
            exchange.response.setComplete()
        } else {
            chain.filter(exchange)
        }
}

suspend fun <T> rateLimited(
    rateLimiter: ApiRateLimiter,
    block: suspend () -> T,
): T {
    if (!rateLimiter.tryConsume()) {
        throw RateLimitExceededException("Rate limit exceeded. Try again later.")
    }
    return block()
}
