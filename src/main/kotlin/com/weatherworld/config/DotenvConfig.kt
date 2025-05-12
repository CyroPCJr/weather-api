package com.weatherworld.config

import io.github.cdimascio.dotenv.dotenv
import org.springframework.context.annotation.Configuration

@Configuration
class DotenvConfig {
    init {
        val dotenv = dotenv()
        dotenv.entries().forEach { entry ->
            System.setProperty(entry.key, entry.value)
        }
    }
}
