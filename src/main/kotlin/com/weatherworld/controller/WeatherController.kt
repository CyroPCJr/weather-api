package com.weatherworld.controller

import com.weatherworld.annotation.RateLimited
import com.weatherworld.model.TemperatureUnit
import com.weatherworld.service.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/weather")
class WeatherController(
    private val weatherService: WeatherService,
) {
    companion object {
        private const val MAX_CITIES_PER_REQUEST = 5
    }

    @GetMapping("/by-city")
    fun getWeather(
        @RequestParam city: String,
        @RequestParam(defaultValue = "METRIC") units: TemperatureUnit,
    ): ResponseEntity<Any> = ResponseEntity.ok(weatherService.getWeather(city))

    @RateLimited
    @GetMapping("/by-coordinates")
    fun getWeatherByCoordinates(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam(defaultValue = "METRIC") units: TemperatureUnit,
    ): ResponseEntity<Any> = ResponseEntity.ok(weatherService.getWeatherByCoordinates(lat, lon, units))

    @RateLimited
    @GetMapping("/by-cities")
    fun getWeatherByCities(
        @RequestParam cities: List<String>,
        @RequestParam(defaultValue = "METRIC") units: TemperatureUnit,
    ): ResponseEntity<Any> =
        runBlocking {
            if (cities.size > MAX_CITIES_PER_REQUEST) {
                return@runBlocking ResponseEntity.badRequest().body("Maximum $MAX_CITIES_PER_REQUEST cities per call.")
            }

            return@runBlocking withContext(Dispatchers.Default) {
                val responseWeather = weatherService.getCachedWeatherByCities(cities, units)
                ResponseEntity.ok(responseWeather)
            }
        }

    @GetMapping("/units")
    fun getSupportedUnits(): List<String> = TemperatureUnit.entries.map { it.name }
}
