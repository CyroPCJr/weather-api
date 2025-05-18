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
import io.mockk.coVerify
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@WebMvcTest(WeatherController::class)
@AutoConfigureMockMvc
class WeatherControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var weatherService: WeatherService

    @MockkBean
    private lateinit var rateLimiter: ApiRateLimiter

    @Test
    fun `should return weather when rate limit not exceeded`() {
        val city = "São Paulo"
        val units = TemperatureUnit.METRIC

        val mockResponse =
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
                name = city,
                cod = 200,
            )

        every { rateLimiter.tryConsume() } returns true
        every { weatherService.getWeather(city, units) } returns mockResponse

        val mockRequest =
            MockMvcRequestBuilders.get("/api/weather/by-city").param("city", city).param("units", units.name)

        mockMvc
            .perform(
                mockRequest,
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value(mockResponse.name))
    }

    @Test
    fun `should return 429 when rate limit exceeded`() {
        every { rateLimiter.tryConsume() } returns false
        val mockRequest =
            MockMvcRequestBuilders.get("/api/weather/by-city").param("city", "Piracicaba").param(
                "units",
                TemperatureUnit.METRIC.name,
            )
        mockMvc
            .perform(
                mockRequest,
            ).andExpect(status().isTooManyRequests)
            .andExpect(content().string("{\"cod\":\"429\",\"message\":\"Rate limit exceeded. Try again later.\"}"))
    }

    @Test
    fun `should return supported temperature units`() {
        val mockRequest = MockMvcRequestBuilders.get("/api/weather/units")
        mockMvc
            .perform(mockRequest)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(TemperatureUnit.entries.size))
    }

    @Test
    fun `should return weather by coordinates when rate limit not exceeded`() {
        val lat = -46.6361
        val lon = -23.5475
        val units = TemperatureUnit.METRIC

        val mockResponse =
            OpenWeatherApiResponse(
                coord = Coordinates(lon = lon, lat = lat),
                weather =
                    listOf(
                        Weather(id = 803, main = "Clouds", description = "broken clouds", icon = "04d"),
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
                wind = Wind(speed = 4.35, deg = 125, gust = 5.55),
                rain = null,
                clouds = Clouds(all = 82),
                dt = 1747059178,
                sys = Sys(type = null, id = null, country = "BR", sunrise = 1747042492, sunset = 1747082348),
                timezone = -10800,
                id = 3453643,
                name = "São Paulo",
                cod = 200,
            )

        every { rateLimiter.tryConsume() } returns true
        every { weatherService.getWeatherByCoordinates(lat, lon, units) } returns mockResponse

        val mockRequest =
            MockMvcRequestBuilders
                .get("/api/weather/by-coordinates")
                .param("lat", lat.toString())
                .param("lon", lon.toString())
                .param("units", units.name)

        mockMvc
            .perform(mockRequest)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.coord.lat").value(lat))
            .andExpect(jsonPath("$.coord.lon").value(lon))
    }

    @Test
    fun `should return 200 OK with weather data for valid cities`() {
        val cities = listOf("São Paulo", "Rio de Janeiro")
        val dummyArrayWeather =
            listOf(
                OpenWeatherApiResponse(
                    coord = Coordinates(lon = -46.6361, lat = -23.5475),
                    weather =
                        listOf(
                            Weather(id = 803, main = "Clouds", description = "broken clouds", icon = "04d"),
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
                    wind = Wind(speed = 4.35, deg = 125, gust = 5.55),
                    rain = null,
                    clouds = Clouds(all = 82),
                    dt = 1747059178,
                    sys = Sys(type = null, id = null, country = "BR", sunrise = 1747042492, sunset = 1747082348),
                    timezone = -10800,
                    id = 3453643,
                    name = "São Paulo",
                    cod = 200,
                ),
                OpenWeatherApiResponse(
                    coord = Coordinates(lon = -43.2075, lat = -22.9028),
                    weather =
                        listOf(
                            Weather(id = 803, main = "Clouds", description = "broken clouds", icon = "04d"),
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
                    wind = Wind(speed = 4.35, deg = 125, gust = 5.55),
                    rain = null,
                    clouds = Clouds(all = 82),
                    dt = 1747059178,
                    sys = Sys(type = null, id = null, country = "BR", sunrise = 1747042492, sunset = 1747082348),
                    timezone = -10800,
                    id = 3453643,
                    name = "Rio de Janeiro",
                    cod = 200,
                ),
            )

        every { rateLimiter.tryConsume() } returns true
        coEvery { weatherService.getCachedWeatherByCities(cities) } returns dummyArrayWeather

        val mockRequest =
            MockMvcRequestBuilders
                .get("/api/weather/by-cities")
                .param("cities", *cities.toTypedArray())

        val result =
            mockMvc
                .perform(mockRequest)
                .andExpect(status().isOk)
                .andReturn()

        println(result.response.contentAsString)
        coVerify(exactly = 1) { weatherService.getCachedWeatherByCities(cities, TemperatureUnit.METRIC) }
    }

    @Test
    fun `should return 400 Bad Request if more than 5 cities`() {
        val cities = List(6) { "City$it" }

        val mockRequest =
            MockMvcRequestBuilders
                .get("/api/weather/by-cities")
                .param("cities", *cities.toTypedArray())

        val result =
            mockMvc
                .perform(mockRequest)
                .andExpect(status().isBadRequest)
                .andReturn()

        println(result.response.contentAsString)
    }

    @Test
    fun `should return 429 Too Many Requests when rate limit is exceeded`() {
        every { rateLimiter.tryConsume() } returns false

        val mockRequest =
            MockMvcRequestBuilders
                .get("/api/weather/by-cities")
                .param("cities", "São Paulo")

        val result =
            mockMvc
                .perform(mockRequest)
                .andExpect(status().isTooManyRequests)
                .andReturn()

        println(result.response.contentAsString)
    }
}
