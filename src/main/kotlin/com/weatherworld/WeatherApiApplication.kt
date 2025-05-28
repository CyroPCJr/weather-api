package com.weatherworld

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
final class WeatherApiApplication

fun main(args: Array<String>) {
    runApplication<WeatherApiApplication>(*args)
}
