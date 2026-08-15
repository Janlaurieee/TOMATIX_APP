package com.tomatix.app.data.repository

import com.tomatix.app.data.firebase.FirebaseService
import com.tomatix.app.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor(
    private val firebaseService: FirebaseService
) {
    fun getSensorData(): Flow<SensorData> = firebaseService.getSensorData()

    fun getDeviceStatus(): Flow<DeviceStatus> = firebaseService.getDeviceStatus()

    fun getThresholdSettings(): Flow<ThresholdSettings> = firebaseService.getThresholdSettings()

    fun getNotificationSettings(): Flow<NotificationSettings> = firebaseService.getNotificationSettings()

    fun getLogs(): Flow<List<SystemLog>> = firebaseService.getLogs()

    suspend fun updateSensorData(data: SensorData) = firebaseService.updateSensorData(data)

    suspend fun updateDeviceStatus(data: DeviceStatus) = firebaseService.updateDeviceStatus(data)

    suspend fun updateThresholdSettings(data: ThresholdSettings) = firebaseService.updateThresholdSettings(data)

    suspend fun updateNotificationSettings(data: NotificationSettings) = firebaseService.updateNotificationSettings(data)

    suspend fun addLog(log: SystemLog) = firebaseService.addLog(log)
}
