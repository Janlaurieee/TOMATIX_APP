package com.tomatix.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tomatix.app.data.model.NotificationSettings
import com.tomatix.app.data.model.SystemLog
import com.tomatix.app.data.model.ThresholdSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.util.Random
import javax.inject.Inject

data class SensorAnalytics(
    val name: String,
    val avg: Double,
    val max: Double,
    val min: Double,
    val chartData: List<Float>
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    var thresholds by mutableStateOf(ThresholdSettings())
        private set

    var notifications by mutableStateOf(NotificationSettings())
        private set

    var selectedTab by mutableIntStateOf(0)
        private set

    var analyticsRange by mutableStateOf("week")
        private set

    var analyticsDate by mutableStateOf(LocalDate.now())
        private set

    var mockLogs by mutableStateOf(generateMockLogs())
        private set

    val temperatureAnalytics: SensorAnalytics
        get() = generateSensorAnalytics("temperature", thresholds.tempMin, thresholds.tempMax)

    val humidityAnalytics: SensorAnalytics
        get() = generateSensorAnalytics("humidity", thresholds.humidityMin, thresholds.humidityMax)

    val soilMoistureAnalytics: SensorAnalytics
        get() = generateSensorAnalytics("soil", thresholds.soilMoistureMin, thresholds.soilMoistureMax)

    val lightIntensityAnalytics: SensorAnalytics
        get() = generateSensorAnalytics("light", thresholds.lightIntensityMin * 0.8, thresholds.lightIntensityMin * 1.5)

    fun updateThresholds(newThresholds: ThresholdSettings) {
        thresholds = newThresholds
    }

    fun saveThresholds() {}

    fun resetThresholds() {
        thresholds = ThresholdSettings()
    }

    fun updateNotifications(newNotifications: NotificationSettings) {
        notifications = newNotifications
    }

    fun saveNotifications() {}

    fun onTabSelected(tab: Int) {
        selectedTab = tab
    }

    fun onRangeSelected(range: String) {
        analyticsRange = range
    }

    fun onDateSelected(date: LocalDate) {
        analyticsDate = date
    }

    fun exportLogs() {}

    fun clearLogs() {
        mockLogs = emptyList()
    }

    private fun generateSensorAnalytics(seed: String, min: Double, max: Double): SensorAnalytics {
        val range = max - min
        val random = Random(seed.hashCode().toLong() + analyticsDate.toEpochDay())
        val dataPoints = when (analyticsRange) {
            "day" -> 24
            "week" -> 7
            "month" -> 30
            "year" -> 12
            else -> 7
        }
        val values = (1..dataPoints).map {
            min + random.nextDouble() * range
        }
        return SensorAnalytics(
            name = seed,
            avg = values.average(),
            max = values.max(),
            min = values.min(),
            chartData = values.map { it.toFloat() }
        )
    }

    companion object {
        private fun generateMockLogs(): List<SystemLog> {
            val events = listOf(
                "System started successfully",
                "Temperature sensor reading: 24.5C",
                "Humidity threshold exceeded",
                "Fan speed adjusted to 75%",
                "Irrigation system activated",
                "Warning: Soil moisture below threshold",
                "Camera snapshot saved",
                "Critical: Temperature sensor failure",
                "Notification sent to admin",
                "System backup completed",
                "Light intensity adjusted",
                "pH level sensor calibrated",
                "Error: Failed to connect to device",
                "System update available",
                "Water pump activated"
            )
            val types = listOf("info", "success", "warning", "error", "info", "info", "success")
            return events.mapIndexed { index, event ->
                SystemLog(
                    id = "log_${index + 1}",
                    time = "${9 + index / 3}:${String.format("%02d", (index * 17) % 60)}",
                    event = event,
                    type = types[index % types.size]
                )
            }
        }
    }
}
