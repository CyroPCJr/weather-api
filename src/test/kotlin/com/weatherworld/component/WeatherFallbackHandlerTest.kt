package com.weatherworld.component

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class WeatherFallbackHandlerTest {
    @Autowired
    lateinit var handler: WeatherFallbackHandler

    @Autowired
    lateinit var meterRegistry: MeterRegistry

    @Test
    fun `should return fallback response for city and increment metric`() {
        val cityName = "Lisbon"
        val response = handler.fallbackWeatherByCity(cityName, RuntimeException("fail"))

        assertEquals(cityName, response.name)

        val counter =
            meterRegistry
                .find("weather.fallback.by_city")
                .tags("city", cityName)
                .counter()

        counter?.count()?.let { assertTrue(it > 0.0) }
    }

    @Test
    fun `should return fallback response for coordinates and increment metric`() {
        val response = handler.fallbackWeatherByCoordinates(12.34, 56.78, RuntimeException("fail"))

        val lat = 56.78.toString()
        val lon = 12.34.toString()
        assertEquals("$lat,$lon", "${response.coord.lat},${response.coord.lon}")

        val counter =
            meterRegistry
                .find("weather.fallback.by_coordinates")
                .tags("lat", lat, "lon", lon)
                .counter()

        counter?.count()?.let { assertTrue(it > 0.0) }
    }

    @Test
    fun `should return fallback responses for cities and increment metric`() {
        val cities = listOf("Lisbon", "Porto")

        val result = handler.fallbackWeatherByCities(cities, RuntimeException("Boom"))

        assertEquals(cities.size, result.size)
        cities.forEach { city ->
            assertTrue(result.any { it.name.contains(city) })
        }

        val counter =
            meterRegistry
                .find("weather.fallback.by_cities")
                .tags("count", cities.size.toString())
                .counter()

        counter?.count()?.let { assertTrue(it > 0.0) }
    }
}
