package com.weatherworld.controller

import com.weatherworld.model.TemperatureUnit
import com.weatherworld.model.dto.OpenWeatherApiResponse
import com.weatherworld.service.WeatherService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/weather")
class WeatherController(
    private val weatherService: WeatherService,
) {
    @GetMapping
    fun getWeather(
        @RequestParam city: String,
        @RequestParam(defaultValue = "METRIC") units: TemperatureUnit,
    ): OpenWeatherApiResponse = weatherService.getWeather(city, units)
}
