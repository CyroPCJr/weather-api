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
        @RequestParam("q") city: String,
        @RequestParam("appid") apiKey: String,
        @RequestParam("units") temperatureUnit: TemperatureUnit = TemperatureUnit.METRIC,
    ): OpenWeatherApiResponse
}
