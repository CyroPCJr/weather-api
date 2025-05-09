package com.weatherworld.service

import com.weatherworld.client.WeatherApiClient
import com.weatherworld.model.TemperatureUnit
import com.weatherworld.model.dto.OpenWeatherApiResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
final class WeatherService(
    private val weatherApiClient: WeatherApiClient,
    @Value("\${weather.api.key}") private val apiKey: String,
) {
    final fun getWeather(
        city: String,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): OpenWeatherApiResponse = weatherApiClient.getWeatherByCity(city, apiKey, units)
}
