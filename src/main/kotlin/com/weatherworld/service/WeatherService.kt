package com.weatherworld.service

import com.weatherworld.client.WeatherApiClient
import com.weatherworld.config.CacheNames
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
    @Cacheable(CacheNames.WEATHER_BY_CITY)
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

    @Cacheable(CacheNames.WEATHER_BY_COORDINATES)
    fun getWeatherByCoordinates(
        lat: Double,
        lon: Double,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): OpenWeatherApiResponse =
        try {
            weatherApiClient.getWeatherByCoordinates(lat, lon, apiKey, units)
        } catch (_: FeignException.NotFound) {
            throw CityNotFoundException("lat=$lat, lon=$lon")
        } catch (ex: FeignException) {
            throw ExternalApiException("Error calling weather API: ${ex.message}")
        }

    @Cacheable(CacheNames.WEATHER_BY_CITIES)
    suspend fun getWeatherByCities(
        cities: List<String>,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): List<OpenWeatherApiResponse> =
        try {
            weatherApiClient.getWeatherByCities(cities, apiKey, units)
        } catch (ex: FeignException.NotFound) {
            throw CityNotFoundException("cities: ${ex.message}")
        } catch (ex: FeignException) {
            throw ExternalApiException("Error calling weather API: ${ex.message}")
        }
}
