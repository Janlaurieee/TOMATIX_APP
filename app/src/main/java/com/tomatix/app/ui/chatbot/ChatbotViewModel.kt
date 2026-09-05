package com.tomatix.app.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomatix.app.data.gemini.AiResult
import com.tomatix.app.data.gemini.GeminiChatMessage
import com.tomatix.app.data.gemini.GeminiService
import com.tomatix.app.data.local.AppPreferences
import com.tomatix.app.data.model.ChatMessage
import com.tomatix.app.data.model.SensorData
import com.tomatix.app.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuickAction(
    val label: String,
    val query: String
)

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val repository: SensorRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isOpen = MutableStateFlow(false)
    val isOpen: StateFlow<Boolean> = _isOpen.asStateFlow()

    private val _inputValue = MutableStateFlow("")
    val inputValue: StateFlow<String> = _inputValue.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _latestSensorData = MutableStateFlow<SensorData?>(null)

    val quickActions = listOf(
        QuickAction("System Status", "What is the current system status?"),
        QuickAction("Crop Advice", "What crops should I plant?"),
        QuickAction("Soil Check", "How is the soil moisture?"),
        QuickAction("Temperature", "What is the temperature?"),
        QuickAction("Growth Tips", "Give me growth tips")
    )

    init {
        val savedMessages = appPreferences.loadChatMessages()
        _messages.value = savedMessages.ifEmpty { listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "Hello! I'm Tomi, your greenhouse assistant. Ask me about your plants or live sensor readings in English, Filipino, Bisaya, Hiligaynon, or Kinaray-a (Karay-a). You can also ask me to switch languages. How can I help you today?",
                sender = "bot",
                timestamp = System.currentTimeMillis()
            )
        ) }

        viewModelScope.launch {
            repository.getSensorData().collect { data ->
                _latestSensorData.value = data
            }
        }
    }

    fun toggleOpen() {
        _isOpen.update { !it }
    }

    fun updateInput(value: String) {
        _inputValue.update { value }
    }

    fun sendMessage() {
        val text = _inputValue.value.trim()
        if (text.isEmpty() || _isTyping.value) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            sender = "user",
            timestamp = System.currentTimeMillis()
        )
        val currentMessages = _messages.value + userMessage
        _messages.value = currentMessages
        appPreferences.saveChatMessages(currentMessages)
        _inputValue.value = ""
        _isTyping.value = true

        viewModelScope.launch {
            val response = when (
                val aiResult = geminiService.generateContent(
                    messages = buildConversation(currentMessages),
                    liveSensorContext = buildLiveSensorContext(_latestSensorData.value)
                )
            ) {
                is AiResult.Success -> ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = aiResult.text,
                    sender = "bot",
                    timestamp = System.currentTimeMillis()
                )

                AiResult.ConfigurationError -> generateLocalResponse(text)

                is AiResult.ApiError -> ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = aiResult.message,
                    sender = "bot",
                    timestamp = System.currentTimeMillis()
                )
            }

            _messages.update { it + response }
            appPreferences.saveChatMessages(_messages.value)
            _isTyping.value = false
        }
    }

    /**
     * Gemini expects alternating `user` and `model` roles. The greeting is local UI copy, so it
     * is deliberately excluded from the request. We also keep the request bounded for speed.
     */
    private fun buildConversation(history: List<ChatMessage>): List<GeminiChatMessage> {
        val recentMessages = history
            .dropWhile { it.sender != "user" }
            .takeLast(MAX_CONVERSATION_MESSAGES)
            .dropWhile { it.sender != "user" }

        return recentMessages.mapNotNull { message ->
            message.text.trim().takeIf { it.isNotEmpty() }?.let { text ->
                GeminiChatMessage(
                    role = if (message.sender == "user") "user" else "model",
                    text = text
                )
            }
        }
    }

    private fun buildLiveSensorContext(data: SensorData?): String {
        if (data == null) {
            return "No sensor reading has been received from Firebase yet. Do not invent live readings."
        }

        val soilSensors = data.soilSensors.take(SOIL_SENSOR_COUNT)
        val individualSoilReadings = if (soilSensors.isEmpty()) {
            "Individual soil probes have not reported yet."
        } else {
            soilSensors.mapIndexed { index, reading ->
                "Sensor ${index + 1}: ${formatReading(reading)}%"
            }.joinToString(", ")
        }

        return """
            Latest synced Firebase readings:
            - Temperature: ${formatReading(data.temperature)} °C
            - Humidity: ${formatReading(data.humidity)}%
            - Incoming sunlight: ${formatReading(data.lightIntensity / 1000.0)} k lux
            - Overall soil moisture: ${formatReading(data.soilMoisture)}%
            - Soil moisture probes (up to four): $individualSoilReadings
        """.trimIndent()
    }

    /** Provides useful answers when no local Gemini key has been configured. */
    private fun generateLocalResponse(query: String): ChatMessage {
        val lower = query.lowercase(Locale.getDefault())
        val data = _latestSensorData.value
        val responseText = when {
            lower.contains("condition") || lower.contains("status") || lower.contains("system") -> {
                if (data == null) {
                    "I have not received a live sensor reading yet. Once Firebase sends one, I can summarize the greenhouse conditions here."
                } else {
                    "Latest greenhouse readings: ${formatReading(data.temperature)}°C, " +
                        "${formatReading(data.humidity)}% humidity, " +
                        "${formatReading(data.soilMoisture)}% overall soil moisture, and " +
                        "${formatReading(data.lightIntensity / 1000.0)} k lux incoming sunlight."
                }
            }

            lower.contains("soil") || lower.contains("moisture") -> localSoilResponse(data)
            lower.contains("temperature") || lower.contains("temp") -> localTemperatureResponse(data)
            lower.contains("growth") || lower.contains("grow") || lower.contains("tip") -> {
                "For steady tomato growth, keep watering consistent, train plants to a support, prune damaged lower leaves, and maintain good airflow. " +
                    data?.let { "Your latest temperature is ${formatReading(it.temperature)}°C and soil moisture is ${formatReading(it.soilMoisture)}%." }.orEmpty()
            }

            lower.contains("crop") || lower.contains("plant") || lower.contains("tomato") -> {
                "Tomatoes are well suited to a managed greenhouse. Use a sunny location, support each plant early, and avoid large swings in soil moisture. " +
                    data?.let { "Your latest soil moisture is ${formatReading(it.soilMoisture)}%." }.orEmpty()
            }

            else -> {
                "I can help with tomato growing, temperature, humidity, sunlight, and the four soil-moisture sensors. " +
                    data?.let { "I currently see ${formatReading(it.temperature)}°C and ${formatReading(it.humidity)}% humidity." }.orEmpty()
            }
        }

        return ChatMessage(
            id = UUID.randomUUID().toString(),
            text = responseText,
            sender = "bot",
            timestamp = System.currentTimeMillis(),
            isAnalysis = data != null
        )
    }

    private fun localSoilResponse(data: SensorData?): String {
        if (data == null) {
            return "I have not received a live soil reading yet. Tomatoes generally do best with evenly moist, well-drained soil rather than letting it swing between very dry and soggy."
        }

        val probes = data.soilSensors.take(SOIL_SENSOR_COUNT)
        val probeText = probes.mapIndexed { index, reading ->
            "S${index + 1} ${formatReading(reading)}%"
        }.joinToString(", ")
        val action = when {
            data.soilMoisture < 45.0 -> "The overall reading is low, so check irrigation and water gradually if the soil is actually dry."
            data.soilMoisture > 70.0 -> "The overall reading is high, so check drainage and avoid adding more water until the root zone has time to dry slightly."
            else -> "The overall reading is within a typical target band; keep the watering schedule consistent."
        }

        return "Latest overall soil moisture is ${formatReading(data.soilMoisture)}%. " +
            (if (probeText.isBlank()) "The individual probes have not reported yet. " else "Probe readings: $probeText. ") +
            action
    }

    private fun localTemperatureResponse(data: SensorData?): String {
        if (data == null) {
            return "I have not received a live temperature reading yet. Tomatoes commonly grow well around 18–28°C, with good ventilation during warmer periods."
        }

        val action = when {
            data.temperature < 18.0 -> "This is cool for active tomato growth; protect plants from cold stress if it continues."
            data.temperature > 28.0 -> "This is warm; increase ventilation or shade during the hottest part of the day if needed."
            else -> "This is within a typical tomato-growing range."
        }
        return "Latest temperature is ${formatReading(data.temperature)}°C. $action"
    }

    private fun formatReading(value: Double): String = String.format(Locale.US, "%.1f", value)

    private companion object {
        const val MAX_CONVERSATION_MESSAGES = 10
        const val SOIL_SENSOR_COUNT = 4
    }
}
