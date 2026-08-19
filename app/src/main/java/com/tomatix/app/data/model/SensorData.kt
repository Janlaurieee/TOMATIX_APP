package com.tomatix.app.data.model

data class SensorData(
    val temperature: Double = 24.5,
    val humidity: Double = 65.0,
    val soilMoisture: Double = 58.0,
    val lightIntensity: Double = 7500.0,
    val soilSensors: List<Double> = emptyList()
)
