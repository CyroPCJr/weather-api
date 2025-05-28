@file:OptIn(ExperimentalCoroutinesApi::class)

package com.weatherworld.service

import com.github.benmanes.caffeine.cache.Cache
import com.weatherworld.component.WeatherFallbackHandler
import com.weatherworld.component.WebClientConfig
import com.weatherworld.component.rateLimited
import com.weatherworld.config.CacheNames
import com.weatherworld.exception.CityNotFoundException
import com.weatherworld.exception.ExternalApiException
import com.weatherworld.model.TemperatureUnit
import com.weatherworld.model.dto.OpenWeatherApiResponse
import com.weatherworld.util.ApiRateLimiter
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody

@Service
class WeatherService(
    @Value("\${weather.api.key}") private val apiKey: String,
    private val fallbackHandler: WeatherFallbackHandler,
    private val webClient: WebClientConfig,
    private val rateLimiter: ApiRateLimiter,
    cacheManager: CacheManager,
    circuitBreakerRegistry: CircuitBreakerRegistry,
) {
    private val weatherByCityCache: Cache<in Any, in Any> =
        (cacheManager.getCache(CacheNames.WEATHER_BY_CITY) as CaffeineCache).nativeCache

    private val weatherByCoordinatesCache: Cache<in Any, in Any> =
        (cacheManager.getCache(CacheNames.WEATHER_BY_COORDINATES) as CaffeineCache).nativeCache

    private val circuitBreaker = circuitBreakerRegistry.circuitBreaker("weatherApi")

    suspend fun getWeather(
        city: String,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): OpenWeatherApiResponse {
        val cacheKey = "${city.lowercase()}:${units.name}"
        weatherByCityCache.getIfPresent(cacheKey)?.let { return it as OpenWeatherApiResponse }

        return runCatching {
            circuitBreaker.executeSuspendFunction {
                fetchWeatherByCity(city, units)
            }
        }.onSuccess {
            weatherByCityCache.put(cacheKey, it)
        }.getOrElse { ex ->
            fallbackHandler.fallbackWeatherByCity(city, ex)
        }
    }

    suspend fun getWeatherByCoordinates(
        lat: Double,
        lon: Double,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): OpenWeatherApiResponse {
        val cacheKey = "$lat:$lon:${units.name}"
        weatherByCoordinatesCache.getIfPresent(cacheKey)?.let { return it as OpenWeatherApiResponse }

        return runCatching {
            circuitBreaker.executeSuspendFunction {
                fetchWeatherByCoordinates(lat, lon, units)
            }
        }.onSuccess {
            weatherByCoordinatesCache.put(cacheKey, it)
        }.getOrElse { ex ->
            fallbackHandler.fallbackWeatherByCoordinates(lon, lat, ex)
        }
    }

    fun getCachedWeatherByCities(
        cities: List<String>,
        units: TemperatureUnit = TemperatureUnit.METRIC,
    ): Flow<OpenWeatherApiResponse> =
        cities
            .asFlow()
            .map { it.lowercase().trim() }
            .flatMapConcat { city ->
                flow {
                    val result =
                        runCatching {
                            circuitBreaker.executeSuspendFunction {
                                getWeather(city, units)
                            }
                        }.getOrElse { ex ->
                            fallbackHandler.fallbackWeatherByCity(city, ex)
                        }
                    emit(result)
                }
            }

    private suspend fun fetchWeatherByCity(
        city: String,
        units: TemperatureUnit,
    ): OpenWeatherApiResponse =
        rateLimited(rateLimiter) {
            try {
                webClient
                    .webClient()
                    .get()
                    .uri { uriBuilder ->
                        uriBuilder
                            .queryParam("q", city)
                            .queryParam("units", units.name)
                            .queryParam("appid", apiKey)
                            .build()
                    }.retrieve()
                    .awaitBody()
            } catch (_: WebClientResponseException.NotFound) {
                throw CityNotFoundException(city)
            } catch (ex: Exception) {
                throw ExternalApiException("Error fetching weather for city $city: ${ex.message}")
            }
        }

    private suspend fun fetchWeatherByCoordinates(
        lat: Double,
        lon: Double,
        units: TemperatureUnit,
    ): OpenWeatherApiResponse =
        rateLimited(rateLimiter) {
            try {
                webClient
                    .webClient()
                    .get()
                    .uri { uriBuilder ->
                        uriBuilder
                            .queryParam("lon", lon)
                            .queryParam("lat", lat)
                            .queryParam("units", units.name.lowercase())
                            .queryParam("appid", apiKey)
                            .build()
                    }.retrieve()
                    .awaitBody()
            } catch (_: WebClientResponseException.NotFound) {
                throw CityNotFoundException("Coordinates not found: $lat,$lon")
            } catch (ex: Exception) {
                throw ExternalApiException("Error fetching weather for coordinates $lat,$lon: ${ex.message}")
            }
        }
}
