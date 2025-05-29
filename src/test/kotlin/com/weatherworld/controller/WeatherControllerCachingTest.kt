package com.weatherworld.controller

import com.ninjasquad.springmockk.MockkBean
import com.weatherworld.model.dto.Clouds
import com.weatherworld.model.dto.Coordinates
import com.weatherworld.model.dto.Main
import com.weatherworld.model.dto.OpenWeatherApiResponse
import com.weatherworld.model.dto.Sys
import com.weatherworld.model.dto.Weather
import com.weatherworld.model.dto.Wind
import com.weatherworld.service.WeatherService
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class WeatherControllerCachingTest {
    private lateinit var mockWebServer: MockWebServer

    @MockkBean
    private lateinit var weatherService: WeatherService

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private val sampleResponse =
        OpenWeatherApiResponse(
            coord =
                Coordinates(
                    lon = -47.6492,
                    lat = -22.7253,
                ),
            weather =
                listOf(
                    Weather(
                        id = 803,
                        main = "Clouds",
                        description = "broken clouds",
                        icon = "04d",
                    ),
                ),
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
            wind =
                Wind(
                    speed = 4.35,
                    deg = 125,
                    gust = 5.55,
                ),
            rain = null,
            clouds =
                Clouds(
                    all = 82,
                ),
            dt = 1747059178,
            sys =
                Sys(
                    type = null,
                    id = null,
                    country = "BR",
                    sunrise = 1747042492,
                    sunset = 1747082348,
                ),
            timezone = -10800,
            id = 3453643,
            name = "Piracicaba",
            cod = 200,
        )

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should return cached response`() =
        runTest {
            val cityName = "Piracicaba"

            coEvery { weatherService.getWeather(cityName, any()) } returns sampleResponse

            webTestClient
                .get()
                .uri("/api/weather/by-city?city=$cityName")
                .exchange()
                .expectStatus()
                .isOk
        }

    @Test
    fun `should cache response after first call`() =
        runTest {
            val cityName = "Piracicaba"

            coEvery { weatherService.getWeather(cityName, any()) } returns sampleResponse

            val firstResponse =
                webTestClient
                    .get()
                    .uri("/api/weather/by-city?city=$cityName")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody()
                    .returnResult()
                    .responseBody

            val secondResponse =
                webTestClient
                    .get()
                    .uri("/api/weather/by-city?city=$cityName")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody()
                    .returnResult()
                    .responseBody

            assertThat(secondResponse).isEqualTo(firstResponse)
        }
}
