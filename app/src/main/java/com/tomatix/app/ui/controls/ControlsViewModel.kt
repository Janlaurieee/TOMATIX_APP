package com.tomatix.app.ui.controls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
class ControlsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ControlsUiState())
    val uiState: StateFlow<ControlsUiState> = _uiState.asStateFlow()

    private var mixingTimerJob: Job? = null

    fun togglePump() {
        _uiState.update { it.copy(pumpStatus = !it.pumpStatus) }
    }

    fun toggleIrrigation() {
        _uiState.update { it.copy(irrigationStatus = !it.irrigationStatus) }
    }

    fun toggleFan() {
        _uiState.update { it.copy(fanStatus = !it.fanStatus) }
    }

    fun toggleCamera() {
        _uiState.update { it.copy(cameraStatus = !it.cameraStatus) }
    }

    fun toggleChemicalMixer() {
        _uiState.update { it.copy(chemicalMixerStatus = !it.chemicalMixerStatus) }
    }

    fun toggleChemicalDistribution() {
        _uiState.update { it.copy(chemicalDistributionStatus = !it.chemicalDistributionStatus) }
    }

    fun setPumpSpeed(speed: Int) {
        _uiState.update { it.copy(pumpSpeed = speed.coerceIn(0, 100)) }
    }

    fun setFanSpeed(speed: Int) {
        _uiState.update { it.copy(fanSpeed = speed.coerceIn(0, 100)) }
    }

    fun setChemicalType(type: String) {
        _uiState.update { it.copy(chemicalType = type) }
    }

    fun setMixingTime(time: Int) {
        _uiState.update { it.copy(mixingTime = time.coerceAtLeast(1)) }
    }

    fun setConcentration(value: Int) {
        _uiState.update { it.copy(concentration = value.coerceIn(0, 100)) }
    }

    fun setCameraZoom(zoom: Int) {
        _uiState.update { it.copy(cameraZoom = zoom.coerceIn(50, 400)) }
    }

    fun startMixing() {
        val state = _uiState.value
        if (state.isMixing) return

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
        }
    }

    fun pauseMixing() {
        mixingTimerJob?.cancel()
        mixingTimerJob = null
        _uiState.update { it.copy(isMixingPaused = true) }
    }

    fun resumeMixing() {
        val state = _uiState.value
        if (!state.isMixingPaused || state.mixingTimeRemaining <= 0) return

        _uiState.update { it.copy(isMixingPaused = false) }

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
        }
    }

    fun stopMixing() {
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
    }

    fun takeSnapshot() {
        // TODO: Implement camera snapshot capture
    }

    fun refreshCamera() {
        // TODO: Implement camera refresh
    }

    fun moveCamera(direction: String) {
        // TODO: Implement camera PTZ controls
    }

    fun triggerIrrigationCycle(cycle: String) {
        _uiState.update { it.copy(irrigationStatus = true) }
        viewModelScope.launch {
            delay(5000L)
            _uiState.update { it.copy(irrigationStatus = false) }
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
    }

    fun resetToDefault() {
        mixingTimerJob?.cancel()
        mixingTimerJob = null
        _uiState.value = ControlsUiState()
    }

    override fun onCleared() {
        super.onCleared()
        mixingTimerJob?.cancel()
    }
}
