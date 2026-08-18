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

    var mockLogs by mutableStateOf(emptyList<SystemLog>())
        private set

    val temperatureAnalytics: SensorAnalytics
        get() = generateSensorAnalytics("temperature")

    val humidityAnalytics: SensorAnalytics
        get() = generateSensorAnalytics("humidity")

    val soilMoistureAnalytics: SensorAnalytics
        get() = generateSensorAnalytics("soil")

    val lightIntensityAnalytics: SensorAnalytics
        get() = generateSensorAnalytics("light")

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

    private fun generateSensorAnalytics(seed: String): SensorAnalytics {
        return SensorAnalytics(
            name = seed,
            avg = 0.0,
            max = 0.0,
            min = 0.0,
            chartData = emptyList()
        )
    }

}
