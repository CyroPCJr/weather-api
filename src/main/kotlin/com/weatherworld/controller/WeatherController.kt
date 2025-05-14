package com.weatherworld.controller

import com.weatherworld.model.TemperatureUnit
import com.weatherworld.service.WeatherService
import com.weatherworld.util.ApiRateLimiter
import com.weatherworld.util.withRateLimit
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/weather")
class WeatherController(
    private val weatherService: WeatherService,
    private val rateLimiter: ApiRateLimiter,
) {
    @GetMapping("/by-city")
    fun getWeather(
        @RequestParam city: String,
        @RequestParam(defaultValue = "METRIC") units: TemperatureUnit,
    ): ResponseEntity<Any> =
        rateLimiter.withRateLimit {
            ResponseEntity.ok(weatherService.getWeather(city, units))
        }

    @GetMapping("/by-coordinates")
    fun getWeatherByCoordinates(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam(defaultValue = "METRIC") units: TemperatureUnit,
    ): ResponseEntity<Any> =
        rateLimiter.withRateLimit {
            ResponseEntity.ok(weatherService.getWeatherByCoordinates(lat, lon, units))
        }

    @GetMapping("/units")
    fun getSupportedUnits(): List<String> = TemperatureUnit.entries.map { it.name }
}
