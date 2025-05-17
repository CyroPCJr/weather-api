package com.weatherworld.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

object CacheNames {
    const val WEATHER_BY_CITY = "weatherByCity"
    const val WEATHER_BY_COORDINATES = "weatherByCoordinates"
    const val WEATHER_BY_CITIES = "weatherByCities"

    fun all(): List<String> = listOf(WEATHER_BY_CITY, WEATHER_BY_COORDINATES, WEATHER_BY_CITIES)
}

@Configuration
class CacheConfig {
    @Bean
    fun caffeineConfig(): Caffeine<Any, Any> =
        Caffeine
            .newBuilder()
            .recordStats()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(100)

    @Bean
    fun cacheManager(caffeine: Caffeine<Any, Any>): CacheManager =
        CaffeineCacheManager().apply {
            println("✅ Cache manager inicializado")
            setCaffeine(caffeine)
            setCacheNames(CacheNames.all())
            println("✅ CacheManager inicializado com caches: ${CacheNames.all()}")
        }
}
