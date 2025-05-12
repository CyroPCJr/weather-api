package com.weatherworld.controller

import com.ninjasquad.springmockk.MockkBean
import com.weatherworld.client.WeatherApiClient
import com.weatherworld.model.dto.Clouds
import com.weatherworld.model.dto.Coordinates
import com.weatherworld.model.dto.Main
import com.weatherworld.model.dto.OpenWeatherApiResponse
import com.weatherworld.model.dto.Sys
import com.weatherworld.model.dto.Weather
import com.weatherworld.model.dto.Wind
import com.weatherworld.service.WeatherService
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.Test

@ExtendWith(SpringExtension::class)
@SpringBootTest
@EnableCaching
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WeatherControllerCachingTest {
    @MockkBean
    private lateinit var apiClient: WeatherApiClient

    @Autowired
    private lateinit var weatherService: WeatherService

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Value("\${weather.api.key}")
    private lateinit var apiKey: String

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
        cacheManager.getCache("weatherByCity")?.clear()
        every { weatherService.getWeather("Piracicaba", any()) } returns sampleResponse
    }

    @Test
    fun `should cache weather response for the same city`() {
        val response1 = weatherService.getWeather("Piracicaba")
        val response2 = weatherService.getWeather("Piracicaba")

        assertThat(response1).isEqualTo(response2)

        verify(exactly = 1) { apiClient.getWeatherByCity("Piracicaba", apiKey) }
    }
}
