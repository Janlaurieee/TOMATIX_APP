package com.tomatix.app.data.repository

import com.tomatix.app.data.firebase.FirebaseService
import com.tomatix.app.data.local.AppPreferences
import com.tomatix.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor(
    private val firebaseService: FirebaseService,
    private val appPreferences: AppPreferences
) {
    fun getSensorData(): Flow<SensorData> = firebaseService.getSensorData()

    fun getDeviceStatus(): Flow<DeviceStatus> = firebaseService.getDeviceStatus()

    fun getThresholdSettings(): Flow<ThresholdSettings> = merge(
        appPreferences.thresholds,
        firebaseService.getThresholdSettings().onEach(appPreferences::saveThresholds)
    )

    fun getNotificationSettings(): Flow<NotificationSettings> = merge(
        appPreferences.notifications,
        firebaseService.getNotificationSettings().onEach(appPreferences::saveNotifications)
    )

    fun getLogs(): Flow<List<SystemLog>> = firebaseService.getLogs()

    suspend fun updateSensorData(data: SensorData) = firebaseService.updateSensorData(data)

    suspend fun updateDeviceStatus(data: DeviceStatus) = firebaseService.updateDeviceStatus(data)

    fun saveThresholdSettingsLocally(data: ThresholdSettings) = appPreferences.saveThresholds(data)

    suspend fun updateThresholdSettings(data: ThresholdSettings) {
        appPreferences.saveThresholds(data)
        firebaseService.updateThresholdSettings(data)
    }

    suspend fun updateNotificationSettings(data: NotificationSettings) {
        appPreferences.saveNotifications(data)
        firebaseService.updateNotificationSettings(data)
    }

    suspend fun addLog(log: SystemLog) = firebaseService.addLog(log)

    suspend fun clearLogs() = firebaseService.clearLogs()
}
