package com.tomatix.app.ui.controls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomatix.app.data.model.SystemLog
import com.tomatix.app.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ControlsUiState(
    val pumpStatus: Boolean = true,
    val irrigationStatus: Boolean = false,
    val fanStatus: Boolean = true,
    val cameraStatus: Boolean = true,
    val chemicalMixerStatus: Boolean = false,
    val chemicalDistributionStatus: Boolean = false,
    val pumpSpeed: Int = 50,
    val fanSpeed: Int = 60,
    val chemicalType: String = "NPK Solution",
    val mixingTime: Int = 30,
    val concentration: Int = 25,
    val cameraZoom: Int = 100,
    val mixingTimeRemaining: Int = 0,
    val isMixing: Boolean = false,
    val isMixingPaused: Boolean = false
)

@HiltViewModel
class ControlsViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ControlsUiState())
    val uiState: StateFlow<ControlsUiState> = _uiState.asStateFlow()

    private var mixingTimerJob: Job? = null

    fun togglePump() {
        _uiState.update { it.copy(pumpStatus = !it.pumpStatus) }
        logChange("Water pump turned ${if (_uiState.value.pumpStatus) "on" else "off"}")
    }

    fun toggleIrrigation() {
        _uiState.update { it.copy(irrigationStatus = !it.irrigationStatus) }
        logChange("Irrigation turned ${if (_uiState.value.irrigationStatus) "on" else "off"}")
    }

    fun toggleFan() {
        _uiState.update { it.copy(fanStatus = !it.fanStatus) }
        logChange("Exhaust fan turned ${if (_uiState.value.fanStatus) "on" else "off"}")
    }

    fun toggleCamera() {
        _uiState.update { it.copy(cameraStatus = !it.cameraStatus) }
        logChange("Camera turned ${if (_uiState.value.cameraStatus) "on" else "off"}")
    }

    fun toggleChemicalMixer() {
        _uiState.update { it.copy(chemicalMixerStatus = !it.chemicalMixerStatus) }
        logChange("Chemical mixer turned ${if (_uiState.value.chemicalMixerStatus) "on" else "off"}")
    }

    fun toggleChemicalDistribution() {
        val turningOn = !_uiState.value.chemicalDistributionStatus
        if (!turningOn) {
            mixingTimerJob?.cancel()
            mixingTimerJob = null
        }
        _uiState.update {
            it.copy(
                chemicalDistributionStatus = turningOn,
                chemicalMixerStatus = if (turningOn) it.chemicalMixerStatus else false,
                isMixing = if (turningOn) it.isMixing else false,
                isMixingPaused = if (turningOn) it.isMixingPaused else false,
                mixingTimeRemaining = if (turningOn) it.mixingTimeRemaining else 0
            )
        }
        logChange("Chemical distribution turned ${if (_uiState.value.chemicalDistributionStatus) "on" else "off"}")
    }

    fun setPumpSpeed(speed: Int) {
        _uiState.update { it.copy(pumpSpeed = speed.coerceIn(0, 100)) }
        logChange("Water pump speed set to ${_uiState.value.pumpSpeed}%")
    }

    fun setFanSpeed(speed: Int) {
        _uiState.update { it.copy(fanSpeed = speed.coerceIn(0, 100)) }
        logChange("Exhaust fan speed set to ${_uiState.value.fanSpeed}%")
    }

    fun setChemicalType(type: String) {
        if (!_uiState.value.chemicalDistributionStatus) return
        _uiState.update { it.copy(chemicalType = type) }
        logChange("Chemical type changed to $type")
    }

    fun setMixingTime(time: Int) {
        if (!_uiState.value.chemicalDistributionStatus) return
        _uiState.update { it.copy(mixingTime = time.coerceAtLeast(1)) }
        logChange("Mixing time set to ${_uiState.value.mixingTime} minutes")
    }

    fun setConcentration(value: Int) {
        if (!_uiState.value.chemicalDistributionStatus) return
        _uiState.update { it.copy(concentration = value.coerceIn(0, 100)) }
        logChange("Chemical concentration set to ${_uiState.value.concentration}%")
    }

    fun setCameraZoom(zoom: Int) {
        _uiState.update { it.copy(cameraZoom = zoom.coerceIn(50, 400)) }
        logChange("Camera zoom set to ${_uiState.value.cameraZoom}%")
    }

    fun startMixing() {
        val state = _uiState.value
        if (!state.chemicalDistributionStatus || state.isMixing) return

        val totalSeconds = state.mixingTime * 60
        _uiState.update {
            it.copy(
                isMixing = true,
                isMixingPaused = false,
                mixingTimeRemaining = totalSeconds,
                chemicalMixerStatus = true
            )
        }

        mixingTimerJob?.cancel()
        logChange("Chemical mixing started for ${state.mixingTime} minutes", "success")
        mixingTimerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.update { it.copy(mixingTimeRemaining = remaining) }
            }
            _uiState.update {
                it.copy(
                    isMixing = false,
                    isMixingPaused = false,
                    mixingTimeRemaining = 0,
                    chemicalMixerStatus = false
                )
            }
            logChange("Chemical mixing completed", "success")
        }
    }

    fun pauseMixing() {
        if (!_uiState.value.chemicalDistributionStatus) return
        mixingTimerJob?.cancel()
        mixingTimerJob = null
        _uiState.update { it.copy(isMixingPaused = true) }
        logChange("Chemical mixing paused")
    }

    fun resumeMixing() {
        val state = _uiState.value
        if (!state.chemicalDistributionStatus || !state.isMixingPaused || state.mixingTimeRemaining <= 0) return

        _uiState.update { it.copy(isMixingPaused = false) }
        logChange("Chemical mixing resumed")

        mixingTimerJob?.cancel()
        mixingTimerJob = viewModelScope.launch {
            var remaining = state.mixingTimeRemaining
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.update { it.copy(mixingTimeRemaining = remaining) }
            }
            _uiState.update {
                it.copy(
                    isMixing = false,
                    isMixingPaused = false,
                    mixingTimeRemaining = 0,
                    chemicalMixerStatus = false
                )
            }
            logChange("Chemical mixing completed", "success")
        }
    }

    fun stopMixing() {
        if (!_uiState.value.chemicalDistributionStatus) return
        mixingTimerJob?.cancel()
        mixingTimerJob = null
        _uiState.update {
            it.copy(
                isMixing = false,
                isMixingPaused = false,
                mixingTimeRemaining = 0,
                chemicalMixerStatus = false
            )
        }
        logChange("Chemical mixing stopped")
    }

    fun takeSnapshot() {
        logChange("Camera snapshot requested")
    }

    fun refreshCamera() {
        logChange("Camera feed refreshed")
    }

    fun moveCamera(direction: String) {
        logChange("Camera moved $direction")
    }

    fun triggerIrrigationCycle(cycle: String) {
        _uiState.update { it.copy(irrigationStatus = true) }
        logChange("$cycle irrigation cycle started", "success")
        viewModelScope.launch {
            delay(5000L)
            _uiState.update { it.copy(irrigationStatus = false) }
            logChange("$cycle irrigation cycle completed", "success")
        }
    }

    fun stopAll() {
        mixingTimerJob?.cancel()
        mixingTimerJob = null
        _uiState.update {
            it.copy(
                pumpStatus = false,
                irrigationStatus = false,
                fanStatus = false,
                cameraStatus = false,
                chemicalMixerStatus = false,
                chemicalDistributionStatus = false,
                isMixing = false,
                isMixingPaused = false,
                mixingTimeRemaining = 0
            )
        }
        logChange("Emergency stop activated", "warning")
    }

    fun resetToDefault() {
        mixingTimerJob?.cancel()
        mixingTimerJob = null
        _uiState.value = ControlsUiState()
        logChange("Manual controls reset to defaults")
    }

    override fun onCleared() {
        super.onCleared()
        mixingTimerJob?.cancel()
    }

    private fun logChange(event: String, type: String = "info") {
        viewModelScope.launch {
            repository.addLog(
                SystemLog(
                    time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    event = event,
                    type = type
                )
            )
        }
    }
}
