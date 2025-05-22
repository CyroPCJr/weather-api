package com.weatherworld.component

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class WeatherFallbackMetrics(
    private val meterRegistry: MeterRegistry,
) {
    fun recordFallBack(city: String) {
        meterRegistry
            .counter("weatherapi.fallback.calls", "city", city)
            .increment()
    }

    fun recordFallBack(
        lon: Double,
        lat: Double,
    ) {
        meterRegistry
            .counter("weatherapi.fallback.calls", "coordinates", lon.toString(), lat.toString())
            .increment()
    }
}
