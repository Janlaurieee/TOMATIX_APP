package com.tomatix.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomatix.app.data.model.NotificationSettings
import com.tomatix.app.data.model.SensorData
import com.tomatix.app.data.model.SystemLog
import com.tomatix.app.data.model.ThresholdSettings
import com.tomatix.app.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch

data class SensorAnalytics(
    val name: String,
    val avg: Double,
    val max: Double,
    val min: Double,
    val chartData: List<Float>
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {

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

    var logs by mutableStateOf(emptyList<SystemLog>())
        private set

    var lastSensorData by mutableStateOf<SensorData?>(null)
        private set

    init {
        viewModelScope.launch {
            repository.getSensorData().collect { data ->
                lastSensorData = data
            }
        }
        viewModelScope.launch {
            repository.getThresholdSettings().collect { thresholds = it }
        }
        viewModelScope.launch {
            repository.getNotificationSettings().collect { notifications = it }
        }
        viewModelScope.launch {
            repository.getLogs().collect { logs = it }
        }
    }

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
        repository.saveThresholdSettingsLocally(newThresholds)
    }

    fun saveThresholds() {
        viewModelScope.launch {
            repository.updateThresholdSettings(thresholds)
            addLog("Environmental thresholds saved", "success")
        }
    }

    fun resetThresholds() {
        thresholds = ThresholdSettings()
        viewModelScope.launch {
            repository.updateThresholdSettings(thresholds)
            addLog("Environmental thresholds reset to defaults", "info")
        }
    }

    fun updateNotifications(newNotifications: NotificationSettings) {
        if (notifications == newNotifications) return
        notifications = newNotifications
        viewModelScope.launch {
            repository.updateNotificationSettings(newNotifications)
            addLog("Notification preferences changed", "info")
        }
    }

    fun saveNotifications() {
        viewModelScope.launch {
            repository.updateNotificationSettings(notifications)
            addLog("Notification preferences saved", "success")
        }
    }

    fun onTabSelected(tab: Int) {
        selectedTab = tab
    }

    fun onRangeSelected(range: String) {
        analyticsRange = range
    }

    fun onDateSelected(date: LocalDate) {
        analyticsDate = date
    }

    fun exportLogs() {
        viewModelScope.launch { addLog("System logs exported", "info") }
    }

    fun clearLogs() {
        viewModelScope.launch { repository.clearLogs() }
    }

    fun buildSensorCsv(): String {
        val sb = StringBuilder()
        sb.append("Timestamp,Temperature (C),Humidity (%),Incoming Sunlight (k lux),")
        for (i in 1..4) {
            sb.append("sensor$i (%)")
            if (i < 4) sb.append(',')
        }
        sb.append('\n')

        val s = lastSensorData
        val timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
        sb.append(timestamp).append(',')
        sb.append(s?.temperature?.let { formatValue(it) } ?: "").append(',')
        sb.append(s?.humidity?.let { formatValue(it) } ?: "").append(',')
        sb.append(s?.lightIntensity?.let { formatValue(it / 1000.0) } ?: "").append(',')
        for (i in 0 until 4) {
            val value = s?.soilSensors?.getOrNull(i)
            sb.append(value?.let { formatValue(it) } ?: "")
            if (i < 3) sb.append(',')
        }
        sb.append('\n')
        return sb.toString()
    }

    private fun formatValue(value: Double): String =
        String.format(Locale.US, "%.1f", value)

    private suspend fun addLog(event: String, type: String) {
        repository.addLog(
            SystemLog(
                time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                event = event,
                type = type
            )
        )
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
