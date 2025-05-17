package com.weatherworld.service

import com.weatherworld.client.WeatherApiClient
import com.weatherworld.config.CacheNames
import com.weatherworld.exception.CityNotFoundException
import com.weatherworld.exception.ExternalApiException
import com.weatherworld.model.TemperatureUnit
import com.weatherworld.model.dto.OpenWeatherApiResponse
import feign.FeignException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
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
    fun getCachedWeatherByCities(
        cities: List<String>,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): List<OpenWeatherApiResponse> =
        runBlocking {
            getWeatherByCitiesAsync(cities, units)
        }

    suspend fun getWeatherByCitiesAsync(
        cities: List<String>,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): List<OpenWeatherApiResponse> =
        try {
            val normalizedCities = cities.map { it.lowercase().trim() }.sorted()
            coroutineScope {
                normalizedCities.map { city ->
                    async {
                        try {
                            getWeather(city, units)
                        } catch (_: FeignException.NotFound) {
                            throw CityNotFoundException("City not found: $city")
                        } catch (ex: FeignException) {
                            throw ExternalApiException("Error fetching weather for city $city: ${ex.message}")
                        }
                    }
                }
            }.awaitAll()
        } catch (ex: Exception) {
            throw ExternalApiException("Unexpected error while fetching weather data: ${ex.message}")
        }
}
