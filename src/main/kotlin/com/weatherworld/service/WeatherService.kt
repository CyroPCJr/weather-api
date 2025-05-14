package com.weatherworld.service

import com.weatherworld.client.WeatherApiClient
import com.weatherworld.exception.CityNotFoundException
import com.weatherworld.exception.ExternalApiException
import com.weatherworld.model.TemperatureUnit
import com.weatherworld.model.dto.OpenWeatherApiResponse
import feign.FeignException
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class WeatherService(
    private val weatherApiClient: WeatherApiClient,
    @Value("\${weather.api.key}") private val apiKey: String,
) {
    @Cacheable("weatherByCity")
    fun getWeather(
        city: String,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): OpenWeatherApiResponse =
        try {
            weatherApiClient.getWeatherByCity(city, apiKey, units)
        } catch (_: FeignException.NotFound) {
            throw CityNotFoundException(city)
        } catch (ex: FeignException) {
            throw ExternalApiException("Error calling weather API: ${ex.message}")
        }
}
