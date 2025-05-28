package com.weatherworld.controller

import com.ninjasquad.springmockk.MockkBean
import com.weatherworld.model.TemperatureUnit
import com.weatherworld.model.dto.Clouds
import com.weatherworld.model.dto.Coordinates
import com.weatherworld.model.dto.Main
import com.weatherworld.model.dto.OpenWeatherApiResponse
import com.weatherworld.model.dto.Sys
import com.weatherworld.model.dto.Weather
import com.weatherworld.model.dto.Wind
import com.weatherworld.service.WeatherService
import com.weatherworld.util.ApiRateLimiter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.Test

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class RateLimiterControllerTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var weatherService: WeatherService

    @MockkBean
    private lateinit var rateLimiter: ApiRateLimiter

    @Test
    fun `should return 200 OK when rate limit is not exceeded`() =
        runTest {
            val cityName = "São Paulo"
            // Arrange
            every { rateLimiter.tryConsume() } returns true
            coEvery { weatherService.getWeather(any(), any()) } returns OpenWeatherApiResponse.mock(cityName)

            webTestClient
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/api/weather/by-city")
                        .queryParam("city", cityName)
                        .queryParam("units", TemperatureUnit.METRIC.name)
                        .build()
                }.exchange()
                .expectStatus()
                .isOk
        }

    @Test
    fun `should return 429 TOO MANY REQUESTS when rate limit is exceeded`() =
        runTest {
            val cityName = "São Paulo"
            every { rateLimiter.tryConsume() } returns false

            webTestClient
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/api/weather/by-city")
                        .queryParam("city", cityName)
                        .queryParam("units", TemperatureUnit.METRIC.name)
                        .build()
                }.exchange()
                .expectStatus()
                .isEqualTo(429)

            verify(exactly = 1) { rateLimiter.tryConsume() }
        }

    private fun OpenWeatherApiResponse.Companion.mock(cityName: String) =
        OpenWeatherApiResponse(
            coord = Coordinates(lat = -22.7253, lon = -47.6492),
            weather = listOf(Weather(803, "Clouds", "broken clouds", "04d")),
            base = "stations",
            main =
                Main(
                    temp = 22.0,
                    feelsLike = 22.0,
                    tempMin = 22.0,
                    tempMax = 22.0,
                    pressure = 1013,
                    humidity = 80,
                    seaLevel = null,
                    groundLevel = null,
                ),
            visibility = 10000,
            wind = Wind(4.35, 125, 5.55),
            rain = null,
            clouds = Clouds(82),
            dt = 1747059178,
            sys = Sys(null, null, "BR", 1747042492, 1747082348),
            timezone = -10800,
            id = 3453643,
            name = cityName,
            cod = 200,
        )
}
