package com.tomatix.app.data.model

data class DeviceStatus(
    val pumpStatus: Boolean = true,
    val irrigationStatus: Boolean = false,
    val fanStatus: Boolean = true,
    val cameraStatus: Boolean = true,
    val chemicalMixerStatus: Boolean = false,
    val chemicalDistributionStatus: Boolean = false
)
