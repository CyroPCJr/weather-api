package com.weatherworld.component

import com.weatherworld.model.dto.OpenWeatherApiResponse
import com.weatherworld.model.dto.openWeatherResponseDefault
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WeatherFallbackHandler(
    private val meterRegistry: MeterRegistry,
) {
    companion object {
        private val log = LoggerFactory.getLogger(WeatherFallbackHandler::class.java)
    }

    fun fallbackWeatherByCity(
        city: String,
        throwable: Throwable,
    ): OpenWeatherApiResponse {
        meterRegistry.counter("weather.fallback.by_city", "city", city).increment()
        log.warn("Fallback triggered for city=$city due to: ${throwable.javaClass.simpleName} - ${throwable.message}")
        return openWeatherResponseDefault(city)
    }

    fun fallbackWeatherByCoordinates(
        lon: Double,
        lat: Double,
        throwable: Throwable,
    ): OpenWeatherApiResponse {
        meterRegistry.counter("weather.fallback.by_coordinates", "lat", "$lat", "lon", "$lon").increment()
        log.warn("Fallback triggered for coordinates=($lat,$lon) due to: ${throwable.javaClass.simpleName} - ${throwable.message}")
        return openWeatherResponseDefault(lon = lon, lat = lat)
    }

    fun fallbackWeatherByCities(
        cities: List<String>,
        throwable: Throwable,
    ): List<OpenWeatherApiResponse> {
        meterRegistry.counter("weather.fallback.by_cities", "count", cities.size.toString()).increment()
        log.warn("Fallback triggered for multiple cities due to: ${throwable.javaClass.simpleName} - ${throwable.message}")
        return cities.map { city ->
            openWeatherResponseDefault(city)
        }
    }
}
