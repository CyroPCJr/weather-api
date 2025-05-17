package com.weatherworld.controller

import com.weatherworld.config.CacheNames
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/debug/cache")
class CacheDebugController(
    private val cacheManager: CacheManager,
) {
    @GetMapping("/stats-weather-by-cities")
    fun getCacheStats(): ResponseEntity<out Any?> {
        val cache =
            cacheManager.getCache(CacheNames.WEATHER_BY_CITIES) as? CaffeineCache
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cache not found")

        val stats = cache.nativeCache.stats()

        val response =
            mapOf(
                "cacheName" to CacheNames.WEATHER_BY_CITIES,
                "size" to cache.nativeCache.estimatedSize(),
                "hitCount" to stats.hitCount(),
                "missCount" to stats.missCount(),
                "hitRate" to stats.hitRate(),
                "missRate" to stats.missRate(),
                "evictionCount" to stats.evictionCount(),
            )

        return ResponseEntity.ok(response)
    }
}
