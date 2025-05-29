package com.weatherworld.config

import io.github.cdimascio.dotenv.Dotenv
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

@Configuration
class DotenvConfig {
    private val log = LoggerFactory.getLogger(DotenvConfig::class.java)

    @PostConstruct
    fun loadEnv() {
        runCatching {
            Dotenv
                .configure()
                .directory("./")
                .ignoreIfMissing()
                .load()
                .entries()
                .forEach { entry ->
                    System.setProperty(entry.key, entry.value)
                }
        }.onFailure {
            log.error("File .env not found - using system variables: ${it.message}")
        }
    }
}
