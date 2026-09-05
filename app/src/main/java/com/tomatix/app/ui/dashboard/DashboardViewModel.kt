package com.tomatix.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomatix.app.data.model.DeviceStatus
import com.tomatix.app.data.model.SensorData
import com.tomatix.app.data.model.SystemLog
import com.tomatix.app.data.notification.TemperatureAlertManager
import com.tomatix.app.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DataPoint(val hour: Int, val value: Float)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SensorRepository,
    private val temperatureAlertManager: TemperatureAlertManager
) : ViewModel() {

    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData.asStateFlow()

    private val _deviceStatus = MutableStateFlow<DeviceStatus?>(null)
    val deviceStatus: StateFlow<DeviceStatus?> = _deviceStatus.asStateFlow()

    private val temperatureHistory = mutableListOf<DataPoint>()
    private val humidityHistory = mutableListOf<DataPoint>()
    private val soilSensorHistories = List(4) { mutableListOf<DataPoint>() }

    init {
        temperatureAlertManager.start()
        viewModelScope.launch {
            repository.getSensorData().collect { data ->
                _sensorData.value = data
                recordSensorReading(data)
            }
        }
        viewModelScope.launch {
            repository.getDeviceStatus().collect { status ->
                _deviceStatus.value = status
            }
        }
    }

    fun togglePump() {
        updateDeviceStatus("Water pump") { it.copy(pumpStatus = !it.pumpStatus) }
    }

    fun toggleIrrigation() {
        updateDeviceStatus("Irrigation") { it.copy(irrigationStatus = !it.irrigationStatus) }
    }

    fun toggleFan() {
        updateDeviceStatus("Exhaust fan") { it.copy(fanStatus = !it.fanStatus) }
    }

    fun toggleCamera() {
        updateDeviceStatus("Camera") { it.copy(cameraStatus = !it.cameraStatus) }
    }

    private fun updateDeviceStatus(deviceName: String, transform: (DeviceStatus) -> DeviceStatus) {
        val updated = transform(_deviceStatus.value ?: DeviceStatus())
        _deviceStatus.value = updated
        viewModelScope.launch {
            repository.updateDeviceStatus(updated)
            val isOn = when (deviceName) {
                "Water pump" -> updated.pumpStatus
                "Irrigation" -> updated.irrigationStatus
                "Exhaust fan" -> updated.fanStatus
                else -> updated.cameraStatus
            }
            repository.addLog(SystemLog(time = logTime(), event = "$deviceName turned ${if (isOn) "on" else "off"}", type = "info"))
        }
    }

    private fun logTime(): String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    fun getTemperatureHistory(): List<DataPoint> = temperatureHistory.toList()

    fun getHumidityHistory(): List<DataPoint> = humidityHistory.toList()

    fun getSoilSensorHistories(): List<List<DataPoint>> = soilSensorHistories.map { it.toList() }

    private fun recordSensorReading(data: SensorData) {
        val timestamp = (System.currentTimeMillis() / 1000L).toInt()
        addDataPoint(temperatureHistory, DataPoint(timestamp, data.temperature.toFloat()))
        addDataPoint(humidityHistory, DataPoint(timestamp, data.humidity.toFloat()))
        data.soilSensors.take(4).forEachIndexed { index, value ->
            addDataPoint(soilSensorHistories[index], DataPoint(timestamp, value.toFloat()))
        }
    }

    private fun addDataPoint(history: MutableList<DataPoint>, point: DataPoint) {
        history += point
        if (history.size > 24) history.removeAt(0)
    }

    fun getTrend(value: Float?, idealMin: Float, idealMax: Float): TrendDirection =
        if (value == null) TrendDirection.STABLE else {
            val mid = (idealMin + idealMax) / 2f
            val threshold = (idealMax - idealMin) * 0.15f
            when {
                value > mid + threshold -> TrendDirection.UP
                value < mid - threshold -> TrendDirection.DOWN
                else -> TrendDirection.STABLE
            }
        }
}

enum class TrendDirection {
    UP, DOWN, STABLE
}
