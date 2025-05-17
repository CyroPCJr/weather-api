package com.weatherworld.exception

class RateLimitExceededException(
    message: String,
) : RuntimeException(message)
