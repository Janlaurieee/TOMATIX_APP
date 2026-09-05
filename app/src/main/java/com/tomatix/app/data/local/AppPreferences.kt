package com.tomatix.app.data.local

import android.content.Context
import androidx.core.content.edit
import com.tomatix.app.data.model.ChatMessage
import com.tomatix.app.data.model.NotificationSettings
import com.tomatix.app.data.model.ThresholdSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Durable, device-local storage for user-owned app state. */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _thresholds = MutableStateFlow(readThresholds())
    val thresholds: StateFlow<ThresholdSettings> = _thresholds

    private val _notifications = MutableStateFlow(readNotifications())
    val notifications: StateFlow<NotificationSettings> = _notifications

    fun saveThresholds(value: ThresholdSettings) {
        preferences.edit(commit = true) {
            putLong(KEY_TEMP_MIN, value.tempMin.toBits())
            putLong(KEY_TEMP_MAX, value.tempMax.toBits())
            putLong(KEY_HUMIDITY_MIN, value.humidityMin.toBits())
            putLong(KEY_HUMIDITY_MAX, value.humidityMax.toBits())
            putLong(KEY_SOIL_MIN, value.soilMoistureMin.toBits())
            putLong(KEY_SOIL_MAX, value.soilMoistureMax.toBits())
            putLong(KEY_LIGHT_MIN, value.lightIntensityMin.toBits())
            putLong(KEY_LIGHT_MAX, value.lightIntensityMax.toBits())
        }
        _thresholds.value = value
    }

    fun saveNotifications(value: NotificationSettings) {
        preferences.edit(commit = true) {
            putBoolean(KEY_EMAIL, value.email)
            putBoolean(KEY_PUSH, value.push)
            putBoolean(KEY_SMS, value.sms)
            putBoolean(KEY_CRITICAL_ONLY, value.criticalOnly)
        }
        _notifications.value = value
    }

    fun loadChatMessages(): List<ChatMessage> {
        val json = preferences.getString(KEY_CHAT_MESSAGES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        ChatMessage(
                            id = item.optString("id"),
                            text = item.optString("text"),
                            sender = item.optString("sender", "user"),
                            timestamp = item.optLong("timestamp"),
                            recommendations = item.optJSONArray("recommendations")?.let { recommendations ->
                                buildList {
                                    for (recommendationIndex in 0 until recommendations.length()) {
                                        add(recommendations.optString(recommendationIndex))
                                    }
                                }
                            }.orEmpty(),
                            isAnalysis = item.optBoolean("isAnalysis")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveChatMessages(messages: List<ChatMessage>) {
        val array = JSONArray()
        messages.takeLast(MAX_STORED_CHAT_MESSAGES).forEach { message ->
            array.put(
                JSONObject().apply {
                    put("id", message.id)
                    put("text", message.text)
                    put("sender", message.sender)
                    put("timestamp", message.timestamp)
                    put("recommendations", JSONArray(message.recommendations))
                    put("isAnalysis", message.isAnalysis)
                }
            )
        }
        preferences.edit(commit = true) { putString(KEY_CHAT_MESSAGES, array.toString()) }
    }

    private fun readThresholds(): ThresholdSettings {
        val defaults = ThresholdSettings()
        return ThresholdSettings(
            tempMin = readDouble(KEY_TEMP_MIN, defaults.tempMin),
            tempMax = readDouble(KEY_TEMP_MAX, defaults.tempMax),
            humidityMin = readDouble(KEY_HUMIDITY_MIN, defaults.humidityMin),
            humidityMax = readDouble(KEY_HUMIDITY_MAX, defaults.humidityMax),
            soilMoistureMin = readDouble(KEY_SOIL_MIN, defaults.soilMoistureMin),
            soilMoistureMax = readDouble(KEY_SOIL_MAX, defaults.soilMoistureMax),
            lightIntensityMin = readDouble(KEY_LIGHT_MIN, defaults.lightIntensityMin),
            lightIntensityMax = readDouble(KEY_LIGHT_MAX, defaults.lightIntensityMax)
        )
    }

    private fun readNotifications(): NotificationSettings {
        val defaults = NotificationSettings()
        return NotificationSettings(
            email = preferences.getBoolean(KEY_EMAIL, defaults.email),
            push = preferences.getBoolean(KEY_PUSH, defaults.push),
            sms = preferences.getBoolean(KEY_SMS, defaults.sms),
            criticalOnly = preferences.getBoolean(KEY_CRITICAL_ONLY, defaults.criticalOnly)
        )
    }

    private fun readDouble(key: String, default: Double): Double =
        if (preferences.contains(key)) Double.fromBits(preferences.getLong(key, 0L)) else default

    private companion object {
        const val PREFERENCES_NAME = "tomatix_saved_data"
        const val KEY_TEMP_MIN = "threshold_temp_min"
        const val KEY_TEMP_MAX = "threshold_temp_max"
        const val KEY_HUMIDITY_MIN = "threshold_humidity_min"
        const val KEY_HUMIDITY_MAX = "threshold_humidity_max"
        const val KEY_SOIL_MIN = "threshold_soil_min"
        const val KEY_SOIL_MAX = "threshold_soil_max"
        const val KEY_LIGHT_MIN = "threshold_light_min"
        const val KEY_LIGHT_MAX = "threshold_light_max"
        const val KEY_EMAIL = "notification_email"
        const val KEY_PUSH = "notification_push"
        const val KEY_SMS = "notification_sms"
        const val KEY_CRITICAL_ONLY = "notification_critical_only"
        const val KEY_CHAT_MESSAGES = "tomi_chat_messages"
        const val MAX_STORED_CHAT_MESSAGES = 200
    }
}
