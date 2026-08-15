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

    private val database = FirebaseDatabase.getInstance()
    private val sensorsRef = database.getReference("sensors")
    private val devicesRef = database.getReference("devices")
    private val settingsRef = database.getReference("settings")
    private val logsRef = database.getReference("logs")

    fun getSensorData(): Flow<SensorData> = callbackFlow {
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
        sensorsRef.addValueEventListener(listener)
        awaitClose { sensorsRef.removeEventListener(listener) }
    }

    fun getDeviceStatus(): Flow<DeviceStatus> = callbackFlow {
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
        devicesRef.addValueEventListener(listener)
        awaitClose { devicesRef.removeEventListener(listener) }
    }

    fun getThresholdSettings(): Flow<ThresholdSettings> = callbackFlow {
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
        settingsRef.addValueEventListener(listener)
        awaitClose { settingsRef.removeEventListener(listener) }
    }

    fun getNotificationSettings(): Flow<NotificationSettings> = callbackFlow {
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
        settingsRef.addValueEventListener(listener)
        awaitClose { settingsRef.removeEventListener(listener) }
    }

    fun getLogs(): Flow<List<SystemLog>> = callbackFlow {
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
        logsRef.addValueEventListener(listener)
        awaitClose { logsRef.removeEventListener(listener) }
    }

    suspend fun updateSensorData(data: SensorData) {
        sensorsRef.setValue(data)
    }

    suspend fun updateDeviceStatus(data: DeviceStatus) {
        devicesRef.setValue(data)
    }

    suspend fun updateThresholdSettings(data: ThresholdSettings) {
        settingsRef.child("thresholds").setValue(data)
    }

    suspend fun updateNotificationSettings(data: NotificationSettings) {
        settingsRef.child("notifications").setValue(data)
    }

    suspend fun addLog(log: SystemLog) {
        logsRef.push().setValue(log)
    }
}
