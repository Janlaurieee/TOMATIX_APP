package com.tomatix.app.data.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tomatix.app.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseService @Inject constructor() {

    private val database: FirebaseDatabase? = runCatching { FirebaseDatabase.getInstance() }.getOrNull()
    private val sensorsRef get() = database?.getReference("sensors")
    private val devicesRef get() = database?.getReference("devices")
    private val settingsRef get() = database?.getReference("settings")
    private val logsRef get() = database?.getReference("logs")

    fun getSensorData(): Flow<SensorData> = callbackFlow {
        val ref = sensorsRef
        if (ref == null) {
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(SensorData::class.java)
                if (data != null) {
                    trySend(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getDeviceStatus(): Flow<DeviceStatus> = callbackFlow {
        val ref = devicesRef
        if (ref == null) {
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(DeviceStatus::class.java)
                if (data != null) {
                    trySend(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getThresholdSettings(): Flow<ThresholdSettings> = callbackFlow {
        val ref = settingsRef
        if (ref == null) {
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.child("thresholds").getValue(ThresholdSettings::class.java)
                if (data != null) {
                    trySend(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getNotificationSettings(): Flow<NotificationSettings> = callbackFlow {
        val ref = settingsRef
        if (ref == null) {
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.child("notifications").getValue(NotificationSettings::class.java)
                if (data != null) {
                    trySend(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getLogs(): Flow<List<SystemLog>> = callbackFlow {
        val ref = logsRef
        if (ref == null) {
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logs = mutableListOf<SystemLog>()
                for (child in snapshot.children) {
                    val log = child.getValue(SystemLog::class.java)
                    if (log != null) {
                        logs.add(log.copy(id = child.key ?: ""))
                    }
                }
                trySend(logs.sortedByDescending { it.time })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun updateSensorData(data: SensorData) {
        sensorsRef?.setValue(data)
    }

    suspend fun updateDeviceStatus(data: DeviceStatus) {
        devicesRef?.setValue(data)
    }

    suspend fun updateThresholdSettings(data: ThresholdSettings) {
        settingsRef?.child("thresholds")?.setValue(data)
    }

    suspend fun updateNotificationSettings(data: NotificationSettings) {
        settingsRef?.child("notifications")?.setValue(data)
    }

    suspend fun addLog(log: SystemLog) {
        logsRef?.push()?.setValue(log)
    }

    suspend fun clearLogs() {
        logsRef?.removeValue()
    }
}
