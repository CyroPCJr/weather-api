package com.weatherworld.client

import com.weatherworld.model.TemperatureUnit
import com.weatherworld.model.dto.OpenWeatherApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "weatherClient", url = "\${weather.api.url}")
interface WeatherApiClient {
    @GetMapping("/weather")
    fun getWeatherByCity(
        @RequestParam(value = "q") city: String,
        @RequestParam(value = "appid") apiKey: String,
        @RequestParam(value = "units") temperatureUnit: TemperatureUnit = TemperatureUnit.METRIC,
    ): OpenWeatherApiResponse

    @GetMapping("/weather")
    fun getWeatherByCoordinates(
        @RequestParam(value = "lat") lat: Double,
        @RequestParam(value = "lon") lon: Double,
        @RequestParam(value = "appid") apiKey: String,
        @RequestParam(value = "units") temperatureUnit: TemperatureUnit = TemperatureUnit.METRIC,
    ): OpenWeatherApiResponse
}
