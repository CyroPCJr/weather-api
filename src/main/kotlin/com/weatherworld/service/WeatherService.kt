package com.weatherworld.service

import com.weatherworld.annotation.RateLimited
import com.weatherworld.client.WeatherApiClient
import com.weatherworld.component.WeatherFallbackHandler
import com.weatherworld.config.CacheNames
import com.weatherworld.exception.CityNotFoundException
import com.weatherworld.exception.ExternalApiException
import com.weatherworld.model.TemperatureUnit
import com.weatherworld.model.dto.OpenWeatherApiResponse
import feign.FeignException
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
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
    private val fallbackHandler: WeatherFallbackHandler,
) {
    @RateLimited
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "internalFallbackWeatherByCity")
    @Cacheable(CacheNames.WEATHER_BY_CITY)
    fun getWeather(
        city: String,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): OpenWeatherApiResponse = weatherApiClient.getWeatherByCity(city, apiKey, units)

    @CircuitBreaker(name = "weatherApi", fallbackMethod = "internalFallbackWeatherByCoordinate")
    @Cacheable(CacheNames.WEATHER_BY_COORDINATES)
    fun getWeatherByCoordinates(
        lat: Double,
        lon: Double,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): OpenWeatherApiResponse = weatherApiClient.getWeatherByCoordinates(lat, lon, apiKey, units)

    @CircuitBreaker(name = "weatherApi", fallbackMethod = "internalFallbackWeatherByCities")
    @Cacheable(CacheNames.WEATHER_BY_CITIES)
    fun getCachedWeatherByCities(
        cities: List<String>,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): List<OpenWeatherApiResponse> =
        runBlocking {
            getWeatherByCitiesAsync(cities, units)
        }

    private suspend fun getWeatherByCitiesAsync(
        cities: List<String>,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): List<OpenWeatherApiResponse> {
        val normalizedCities = cities.map { it.lowercase().trim() }.sorted()
        return coroutineScope {
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
    }

    private fun internalFallbackWeatherByCity(
        city: String,
        units: TemperatureUnit,
        t: Throwable,
    ): OpenWeatherApiResponse = fallbackHandler.fallbackWeatherByCity(city, units, t)

    private fun internalFallbackWeatherByCoordinate(
        lon: Double,
        lat: Double,
        units: TemperatureUnit,
        t: Throwable,
    ): OpenWeatherApiResponse = fallbackHandler.fallbackWeatherByCoordinates(lon, lat, units, t)

    private fun internalFallbackWeatherByCities(
        cities: List<String>,
        units: TemperatureUnit,
        t: Throwable,
    ): List<OpenWeatherApiResponse> = fallbackHandler.fallbackWeatherByCities(cities, units, t)
}
