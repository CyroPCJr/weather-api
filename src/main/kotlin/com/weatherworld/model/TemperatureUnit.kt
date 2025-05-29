package com.weatherworld.model

enum class TemperatureUnit(
    val value: String,
) {
    STANDARD("standard"),
    METRIC("metric"),
    IMPERIAL("imperial"),
    ;

    override fun toString(): String = value
}
