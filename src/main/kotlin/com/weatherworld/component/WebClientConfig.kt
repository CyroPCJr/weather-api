package com.weatherworld.component

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    companion object {
        private const val WEATHER_API_URL = "https://api.openweathermap.org/data/2.5"
    }

    @Bean
    fun webClient(): WebClient =
        WebClient
            .builder()
            .baseUrl("$WEATHER_API_URL/weather")
            .defaultHeader("Accept", "application/json")
            .build()
}
