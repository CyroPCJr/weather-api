package com.weatherworld.component

import com.weatherworld.exception.CityNotFoundException
import com.weatherworld.exception.FeignErrorHandler
import com.weatherworld.exception.LonLatNotFoundException
import com.weatherworld.model.TemperatureUnit
import com.weatherworld.model.dto.Clouds
import com.weatherworld.model.dto.Coordinates
import com.weatherworld.model.dto.Main
import com.weatherworld.model.dto.OpenWeatherApiResponse
import com.weatherworld.model.dto.Sys
import com.weatherworld.model.dto.Weather
import com.weatherworld.model.dto.Wind
import org.springframework.stereotype.Component

@Component
class WeatherFallbackHandler(
    private val metrics: WeatherFallbackMetrics,
) {
    fun fallbackWeatherByCity(
        city: String,
        units: TemperatureUnit,
        t: Throwable,
    ): OpenWeatherApiResponse {
        try {
            metrics.recordFallBack(city)
        } catch (t: Throwable) {
            FeignErrorHandler.handle(t) {
                throw CityNotFoundException(city)
            }
        }
        return OpenWeatherApiResponse(
            coord = Coordinates(0.0, 0.0),
            weather =
                listOf(
                    Weather(0, "Unavailable", "Service unavailable", "00d"),
                ),
            base = "fallback",
            main = Main(24.21, 0.0, 0.0, 0.0, 0, 0, 0),
            visibility = 0,
            wind = Wind(0.0, 0, 0.0),
            rain = null,
            clouds = Clouds(0),
            dt = 0,
            sys = Sys(null, null, "??", 0, 0),
            timezone = 0,
            id = 0,
            name = city,
            cod = 200,
        )
    }

    fun fallbackWeatherByCoordinates(
        lon: Double,
        lat: Double,
        units: TemperatureUnit,
        ex: Throwable,
    ): OpenWeatherApiResponse {
        try {
            metrics.recordFallBack(lon, lat)
        } catch (e: Throwable) {
            FeignErrorHandler.handle(e) {
                throw LonLatNotFoundException(lon = lon, lat = lat)
            }
        }
        return OpenWeatherApiResponse(
            coord = Coordinates(0.0, 0.0),
            weather =
                listOf(
                    Weather(0, "Unavailable", "Service unavailable", "00d"),
                ),
            base = "fallback",
            main = Main(24.21, 0.0, 0.0, 0.0, 0, 0, 0),
            visibility = 0,
            wind = Wind(0.0, 0, 0.0),
            rain = null,
            clouds = Clouds(0),
            dt = 0,
            sys = Sys(null, null, "??", 0, 0),
            timezone = 0,
            id = 0,
            name = "city",
            cod = 200,
        )
    }

    fun fallbackWeatherByCities(
        cities: List<String>,
        units: TemperatureUnit,
        ex: Throwable,
    ): List<OpenWeatherApiResponse> =
        cities.map { city ->
            fallbackWeatherByCity(city, units, ex)
        }
}
