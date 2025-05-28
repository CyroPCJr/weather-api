package com.weatherworld.controller

import com.weatherworld.model.TemperatureUnit
import com.weatherworld.model.dto.OpenWeatherApiResponse
import com.weatherworld.service.WeatherService
import kotlinx.coroutines.flow.Flow
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
    suspend fun getWeather(
        @RequestParam city: String,
        @RequestParam(defaultValue = "METRIC") units: TemperatureUnit = TemperatureUnit.METRIC,
    ): ResponseEntity<OpenWeatherApiResponse> = ResponseEntity.ok(weatherService.getWeather(city, units))

    @GetMapping("/by-coordinates")
    suspend fun getWeatherByCoordinates(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam(defaultValue = "METRIC") units: TemperatureUnit = TemperatureUnit.METRIC,
    ): ResponseEntity<OpenWeatherApiResponse> = ResponseEntity.ok(weatherService.getWeatherByCoordinates(lat, lon, units))

    @GetMapping("/by-cities")
    fun getWeatherByCities(
        @RequestParam cities: List<String>,
        @RequestParam(defaultValue = "METRIC") units: TemperatureUnit = TemperatureUnit.METRIC,
    ): Flow<OpenWeatherApiResponse> {
        cities.also {
            require(it.size <= MAX_CITIES_PER_REQUEST) {
                "Maximum $MAX_CITIES_PER_REQUEST cities per call."
            }
        }
        return weatherService.getCachedWeatherByCities(cities, units)
    }

    @GetMapping("/units")
    fun getSupportedUnits(): List<String> = TemperatureUnit.entries.map { it.name }
}
