package com.weatherworld.component

import com.weatherworld.model.TemperatureUnit
import com.weatherworld.model.dto.Clouds
import com.weatherworld.model.dto.Coordinates
import com.weatherworld.model.dto.Main
import com.weatherworld.model.dto.OpenWeatherApiResponse
import com.weatherworld.model.dto.Sys
import com.weatherworld.model.dto.Weather
import com.weatherworld.model.dto.Wind
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WeatherFallbackHandler {
    fun fallbackWeatherByCity(
        city: String,
        units: TemperatureUnit,
        ex: Throwable,
    ): OpenWeatherApiResponse {
        println("⚠️ Fallback trigger on city: $city - ${ex.message}")
        return OpenWeatherApiResponse(
            coord = Coordinates(lon = -47.6492, lat = -22.7253),
            weather = listOf(Weather(id = 803, main = "Clouds", description = "broken clouds", icon = "04d")),
            base = "stations",
            main =
                Main(
                    temp = 24.21,
                    feelsLike = 24.41,
                    tempMin = 24.21,
                    tempMax = 24.21,
                    pressure = 1023,
                    humidity = 66,
                    seaLevel = 1023,
                    groundLevel = 959,
                ),
            visibility = 10000,
            wind = Wind(speed = 4.35, deg = 125, gust = 5.55),
            rain = null,
            clouds = Clouds(all = 82),
            dt = Instant.now().epochSecond,
            sys = Sys(type = null, id = null, country = "BR", sunrise = 1747042492, sunset = 1747082348),
            timezone = -10800,
            id = 3453643,
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
        println("⚠️ Fallback trigger on lon: $lon | lat: $lat - ${ex.message}")
        return OpenWeatherApiResponse(
            coord = Coordinates(lon = lon, lat = lat),
            weather = listOf(Weather(id = 803, main = "Clouds", description = "broken clouds", icon = "04d")),
            base = "stations",
            main =
                Main(
                    temp = 24.21,
                    feelsLike = 24.41,
                    tempMin = 24.21,
                    tempMax = 24.21,
                    pressure = 1023,
                    humidity = 66,
                    seaLevel = 1023,
                    groundLevel = 959,
                ),
            visibility = 10000,
            wind = Wind(speed = 4.35, deg = 125, gust = 5.55),
            rain = null,
            clouds = Clouds(all = 82),
            dt = Instant.now().epochSecond,
            sys = Sys(type = null, id = null, country = "BR", sunrise = 1747042492, sunset = 1747082348),
            timezone = -10800,
            id = 3453643,
            name = "city",
            cod = 200,
        )
    }

    fun fallbackWeatherByCities(
        cities: List<String>,
        units: TemperatureUnit,
        ex: Throwable,
    ): List<OpenWeatherApiResponse> {
        println("⚠️ Fallback acionado para múltiplas cidades: $cities - ${ex.message}")
        return cities.map { city ->
            fallbackWeatherByCity(city, units, ex)
        }
    }
}
