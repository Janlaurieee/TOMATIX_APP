package com.tomatix.app.data.model

data class ThresholdSettings(
    val tempMin: Double = 21.0,
    val tempMax: Double = 27.0,
    val humidityMin: Double = 50.0,
    val humidityMax: Double = 70.0,
    val soilMoistureMin: Double = 45.0,
    val soilMoistureMax: Double = 65.0,
    val lightIntensityMin: Double = 5000.0
)
