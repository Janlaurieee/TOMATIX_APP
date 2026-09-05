package com.tomatix.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tomatix.app.MainActivity
import com.tomatix.app.R
import com.tomatix.app.data.model.NotificationSettings
import com.tomatix.app.data.model.SensorData
import com.tomatix.app.data.model.SystemLog
import com.tomatix.app.data.model.ThresholdSettings
import com.tomatix.app.data.repository.SensorRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Singleton
class TemperatureAlertManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SensorRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false
    private var wasBelowMinimum = false
    private var lastNotificationAt = 0L

    fun start() {
        if (started) return
        started = true
        createNotificationChannel()

        scope.launch {
            combine(
                repository.getSensorData(),
                repository.getThresholdSettings().onStart { emit(ThresholdSettings()) },
                repository.getNotificationSettings().onStart { emit(NotificationSettings()) }
            ) { sensorData, thresholds, notificationSettings ->
                Triple(sensorData, thresholds, notificationSettings)
            }.collect { (sensorData, thresholds, notificationSettings) ->
                handleTemperature(sensorData, thresholds, notificationSettings)
            }
        }
    }

    private suspend fun handleTemperature(
        sensorData: SensorData,
        thresholds: ThresholdSettings,
        settings: NotificationSettings
    ) {
        val isLow = sensorData.temperature < thresholds.tempMin
        val now = System.currentTimeMillis()
        val shouldNotify = isLow && settings.push &&
            (!wasBelowMinimum || now - lastNotificationAt >= ALERT_COOLDOWN_MILLIS)

        if (shouldNotify && canPostNotifications()) {
            postLowTemperatureNotification(sensorData.temperature, thresholds.tempMin)
            lastNotificationAt = now
            repository.addLog(
                SystemLog(
                    time = LocalDateTime.now().format(LOG_TIME_FORMAT),
                    event = "Low temperature alert sent: ${formatTemperature(sensorData.temperature)}°C",
                    type = "warning"
                )
            )
        }
        wasBelowMinimum = isLow
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            TEMPERATURE_ALERT_CHANNEL,
            "Temperature alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when greenhouse temperature falls below its minimum threshold."
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun postLowTemperatureNotification(temperature: Double, minimum: Double) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, TEMPERATURE_ALERT_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Low greenhouse temperature")
            .setContentText(
                "Current: ${formatTemperature(temperature)}°C. Minimum: ${formatTemperature(minimum)}°C."
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "The greenhouse is at ${formatTemperature(temperature)}°C, below your minimum of " +
                        "${formatTemperature(minimum)}°C. Check heating and ventilation."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(LOW_TEMPERATURE_NOTIFICATION_ID, notification)
    }

    private fun formatTemperature(value: Double): String = String.format(Locale.US, "%.1f", value)

    private companion object {
        const val TEMPERATURE_ALERT_CHANNEL = "temperature_alerts"
        const val LOW_TEMPERATURE_NOTIFICATION_ID = 2001
        const val ALERT_COOLDOWN_MILLIS = 30 * 60 * 1000L
        val LOG_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
