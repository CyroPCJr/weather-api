package com.weatherworld.exception

class CityNotFoundException(
    city: String,
) : RuntimeException("city: '$city' not found")
