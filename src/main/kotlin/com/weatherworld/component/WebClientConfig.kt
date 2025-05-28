package com.weatherworld.component

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Value("\${weather.api.url}")
    private lateinit var weatherApiUrl: String

    @Bean
    fun webClient(): WebClient =
        WebClient
            .builder()
            .baseUrl("$weatherApiUrl/weather")
            .defaultHeader("Accept", "application/json")
            .build()
}
