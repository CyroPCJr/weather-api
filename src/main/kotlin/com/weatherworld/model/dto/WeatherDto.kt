package com.weatherworld.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherApiResponse(
    val coord: Coordinates,
    val weather: List<Weather>,
    val base: String,
    val main: Main,
    val visibility: Int,
    val wind: Wind,
    val rain: Rain? = null,
    val clouds: Clouds,
    val dt: Long,
    val sys: Sys,
    val timezone: Int,
    val id: Long,
    val name: String,
    val cod: Int,
)

@Serializable
data class Coordinates(
    val lon: Double,
    val lat: Double,
)

@Serializable
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,
)

@Serializable
data class Main(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
    @SerialName("sea_level") val seaLevel: Int? = null,
    @SerialName("grnd_level") val groundLevel: Int? = null,
)

@Serializable
data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double? = null,
)

@Serializable
data class Rain(
    @SerialName("1h") val lastHour: Double? = null,
    @SerialName("3h") val lastThreeHours: Double? = null,
)

@Serializable
data class Clouds(
    val all: Int,
)

@Serializable
data class Sys(
    val type: Int? = null,
    val id: Int? = null,
    val country: String,
    val sunrise: Long,
    val sunset: Long,
)

fun openWeatherResponseDefault(
    city: String = "CityFallback",
    lon: Double = 0.0,
    lat: Double = 0.0,
): OpenWeatherApiResponse =
    OpenWeatherApiResponse(
        coord = Coordinates(lon, lat),
        weather = listOf(Weather(0, "Unavailable", "Service unavailable", "00d")),
        base = "fallback",
        main = Main(0.0, 0.0, 0.0, 0.0, 0, 0, 0),
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
