package com.tomatix.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomatix.app.data.model.DeviceStatus
import com.tomatix.app.data.model.SensorData
import com.tomatix.app.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DataPoint(val hour: Int, val value: Float)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {

    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData.asStateFlow()

    private val _deviceStatus = MutableStateFlow<DeviceStatus?>(null)
    val deviceStatus: StateFlow<DeviceStatus?> = _deviceStatus.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSensorData().collect { data ->
                _sensorData.value = data
            }
        }
        viewModelScope.launch {
            repository.getDeviceStatus().collect { status ->
                _deviceStatus.value = status
            }
        }
    }

    fun getTemperatureHistory(): List<DataPoint> = emptyList()

    fun getHumidityHistory(): List<DataPoint> = emptyList()

    fun getSoilMoistureHistory(): List<DataPoint> = emptyList()

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
