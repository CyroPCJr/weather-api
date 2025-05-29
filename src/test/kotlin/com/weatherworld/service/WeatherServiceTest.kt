package com.weatherworld.service

import com.ninjasquad.springmockk.MockkBean
import com.weatherworld.exception.CityNotFoundException
import com.weatherworld.model.TemperatureUnit
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class WeatherServiceTest {
    @MockkBean
    private lateinit var weatherService: WeatherService

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should return fallback when weatherService throws exception`() =
        runTest {
            val city = "TestCity"

            coEvery {
                weatherService.getWeather(city, TemperatureUnit.METRIC)
            } throws CityNotFoundException(city)

            webTestClient
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/api/weather/by-city")
                        .queryParam("city", city)
                        .queryParam("units", TemperatureUnit.METRIC.name)
                        .build()
                }.exchange()
                .expectStatus()
                .isNotFound
                .expectBody<String>()
                .consumeWith { result ->
                    assertThat(result.responseBody).contains("'$city' not found")
                }
        }
}
