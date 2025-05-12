package com.weatherworld.controller

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
        return if (rateLimiter.tryConsume()) {
            val response = weatherService.getWeather(city, units)
            return ResponseEntity.ok(response)
        } else {
            ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Rate limit exceeded. Try again later.")
        }
    }

    @GetMapping("/units")
    final fun getSupportedUnits(): List<String> = TemperatureUnit.entries.map { it.name }
}
