package com.weatherworld.controller

import com.weatherworld.exception.GlobalExceptionHandler
import com.weatherworld.model.TemperatureUnit
import com.weatherworld.service.WeatherService
import com.weatherworld.util.ApiRateLimiter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/weather")
final class WeatherController(
    private val weatherService: WeatherService,
    private val rateLimiter: ApiRateLimiter,
) {
    @GetMapping
    final fun getWeather(
        @RequestParam city: String,
        @RequestParam(defaultValue = "METRIC") units: TemperatureUnit,
    ): ResponseEntity<Any> {
        if (!rateLimiter.tryConsume()) {
            val error = GlobalExceptionHandler.ErrorResponse("429", "Rate limit exceeded. Try again later.")
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error)
        }

        val response = weatherService.getWeather(city, units)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/units")
    final fun getSupportedUnits(): List<String> = TemperatureUnit.entries.map { it.name }
}
