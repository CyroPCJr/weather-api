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
import io.mockk.coEvery
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class WeatherControllerTest {
    private lateinit var mockWebServer: MockWebServer

    @MockkBean
    private lateinit var weatherService: WeatherService

    @Autowired
    private lateinit var client: WebTestClient

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
    fun `should return data from the city`() =
        runTest {
            val cityName = "London"
            val mockResponse =
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

            coEvery { weatherService.getWeather(cityName, TemperatureUnit.METRIC) } returns mockResponse

            client
                .get()
                .uri("/api/weather/by-city?city=$cityName")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo(cityName)
                .jsonPath("$.main.temp")
                .isEqualTo(22.0)
        }

    @Test
    fun `should return stream of weather data for multiple cities`() =
        runTest {
            val mockResponse1 =
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
                    name = "London",
                    cod = 200,
                )
            val mockResponse2 =
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
                    name = "Berlin",
                    cod = 200,
                )

            coEvery {
                weatherService.getCachedWeatherByCities(listOf("London", "Berlin"), TemperatureUnit.METRIC)
            } returns flowOf(mockResponse1, mockResponse2)

            client
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/api/weather/by-cities")
                        .queryParam("cities", "London", "Berlin")
                        .queryParam("units", "METRIC")
                        .build()
                }.accept(MediaType.APPLICATION_NDJSON) // importante para fluxo!
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
                .expectBodyList(OpenWeatherApiResponse::class.java)
                .hasSize(2)
                .returnResult()
                .responseBody
                ?.let {
                    assertEquals("London", it[0].name)
                    assertEquals("Berlin", it[1].name)
                }
        }
}
