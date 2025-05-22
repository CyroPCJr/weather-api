package com.weatherworld.component

import com.ninjasquad.springmockk.MockkBean
import com.weatherworld.client.WeatherApiClient
import com.weatherworld.model.TemperatureUnit
import feign.FeignException
import feign.Request
import feign.RequestTemplate
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetricsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var meterRegistry: MeterRegistry

    @MockkBean
    lateinit var weatherApiClient: WeatherApiClient

    @Test
    fun `should register fallback metrics and circuit breaker`() {
        val city = "TestCity"

        val request =
            Request.create(
                Request.HttpMethod.GET,
                "http://localhost/mock-endpoint",
                emptyMap(),
                ByteArray(0),
                StandardCharsets.UTF_8,
                RequestTemplate(),
            )

        every {
            weatherApiClient.getWeatherByCity(city, any(), any())
        } throws FeignException.ServiceUnavailable("mock", request, ByteArray(0), emptyMap())

        val responseBody =
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/weather/by-city")
                        .param("city", city)
                        .param("units", TemperatureUnit.METRIC.name),
                ).andExpect(status().isOk)
                .andReturn()

        assertThat(responseBody.response.contentAsString).contains("Service unavailable")

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val fallbackCounter = meterRegistry.counter("weatherapi.fallback.calls", "city", city)
            val cbTimer =
                meterRegistry
                    .find("resilience4j.circuitbreaker.calls")
                    .tags("name", "weatherApi", "kind", "failed")
                    .timer()

            assertThat(fallbackCounter.count()).isGreaterThan(0.0)
            assertThat(cbTimer?.count()).isEqualTo(0L)
        }
    }
}
