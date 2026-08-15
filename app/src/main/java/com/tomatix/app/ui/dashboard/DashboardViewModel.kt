package com.tomatix.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomatix.app.data.model.DeviceStatus
import com.tomatix.app.data.model.SensorData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.round
import kotlin.random.Random

data class DataPoint(val hour: Int, val value: Float)

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    private val _deviceStatus = MutableStateFlow(DeviceStatus())
    val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    init {
        startMockUpdates()
    }

    private fun startMockUpdates() {
        viewModelScope.launch {
            while (true) {
                delay(3000L)
                _sensorData.update {
                    SensorData(
                        temperature = Random.nextDouble(20.0, 30.0).roundTo(1),
                        humidity = Random.nextDouble(40.0, 80.0).roundTo(1),
                        soilMoisture = Random.nextDouble(35.0, 75.0).roundTo(1),
                        lightIntensity = Random.nextDouble(2000.0, 12000.0).roundTo(0)
                    )
                }
                _deviceStatus.update {
                    DeviceStatus(
                        pumpStatus = Random.nextBoolean(),
                        irrigationStatus = Random.nextBoolean(),
                        fanStatus = Random.nextBoolean(),
                        cameraStatus = true
                    )
                }
            }
        }
    }

    fun getTemperatureHistory(): List<DataPoint> {
        val base = 24.5f
        return (0..23).map { hour ->
            DataPoint(hour, base + Random.nextFloat() * 4f - 2f)
        }
    }

    fun getHumidityHistory(): List<DataPoint> {
        val base = 60f
        return (0..23).map { hour ->
            DataPoint(hour, base + Random.nextFloat() * 20f - 10f)
        }
    }

    fun getSoilMoistureHistory(): List<DataPoint> {
        val base = 55f
        return (0..23).map { hour ->
            DataPoint(hour, base + Random.nextFloat() * 16f - 8f)
        }
    }

    fun getTrend(value: Float, idealMin: Float, idealMax: Float): TrendDirection {
        val mid = (idealMin + idealMax) / 2f
        val threshold = (idealMax - idealMin) * 0.15f
        return when {
            value > mid + threshold -> TrendDirection.UP
            value < mid - threshold -> TrendDirection.DOWN
            else -> TrendDirection.STABLE
        }
    }
}

enum class TrendDirection {
    UP, DOWN, STABLE
}

private fun Double.roundTo(decimals: Int): Double {
    val factor = 10.0.pow(decimals.toDouble())
    return round(this * factor) / factor
}
