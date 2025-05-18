package com.weatherworld.service

import com.ninjasquad.springmockk.MockkBean
import com.weatherworld.client.WeatherApiClient
import com.weatherworld.model.TemperatureUnit
import feign.FeignException
import io.mockk.coEvery
import io.mockk.mockk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
class WeatherServiceTest {
    @Autowired
    private lateinit var weatherService: WeatherService

    @MockkBean
    private lateinit var weatherApiClient: WeatherApiClient

    @Test
    fun `should return fallback when weatherApiClient per city`() {
        // given
        val city = "Piracicaba"
        val units = TemperatureUnit.METRIC

        coEvery { weatherApiClient.getWeatherByCity(city, any(), units) } throws
            FeignException.ServiceUnavailable("503", mockk(), null, null)

        // when
        val response = weatherService.getWeather(city, units)

        // then
        assertEquals(city, response.name)
        assertEquals(24.21, response.main.temp)
        assertEquals("Clouds", response.weather.first().main)
        assertEquals("BR", response.sys.country)
        assertEquals(200, response.cod)
    }

    @Test
    fun `should return fallback when weatherApiClient by coordinates `() {
        // given
        val units = TemperatureUnit.METRIC

        coEvery { weatherApiClient.getWeatherByCoordinates(any(), any(), any(), units) } throws
            FeignException.ServiceUnavailable("503", mockk(), null, null)

        // when
        val response = weatherService.getWeatherByCoordinates(22.47, -27.50)

        // then
        assertEquals(24.21, response.main.temp)
        assertEquals("Clouds", response.weather.first().main)
        assertEquals("BR", response.sys.country)
        assertEquals(200, response.cod)
    }
}
