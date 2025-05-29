package com.weatherworld.exception

class LonLatNotFoundException(
    lon: Double,
    lat: Double,
) : RuntimeException("longitude: $lon | latitude: $lat")
