package com.tomatix.app.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomatix.app.data.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class QuickAction(
    val label: String,
    val query: String
)

@HiltViewModel
class ChatbotViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isOpen = MutableStateFlow(false)
    val isOpen: StateFlow<Boolean> = _isOpen.asStateFlow()

    private val _inputValue = MutableStateFlow("")
    val inputValue: StateFlow<String> = _inputValue.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val mockTemp = 24.5
    private val mockHumidity = 65.0
    private val mockSoil = 58.0
    private val mockLight = 7500.0

    val quickActions = listOf(
        QuickAction("System Status", "What is the current system status?"),
        QuickAction("Crop Advice", "What crops should I plant?"),
        QuickAction("Soil Check", "How is the soil moisture?"),
        QuickAction("Temperature", "What is the temperature?"),
        QuickAction("Growth Tips", "Give me growth tips"),
    )

    init {
        _messages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "Hello! I'm your Tomatix AI assistant. I can help you monitor your greenhouse, analyze sensor data, and provide growing recommendations. How can I help you today?",
                sender = "bot",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun toggleOpen() {
        _isOpen.update { !it }
    }

    fun updateInput(value: String) {
        _inputValue.update { value }
    }

    fun sendMessage() {
        val text = _inputValue.value.trim()
        if (text.isEmpty()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            sender = "user",
            timestamp = System.currentTimeMillis()
        )
        _messages.update { it + userMessage }
        _inputValue.update { "" }

        viewModelScope.launch {
            _isTyping.update { true }
            delay(1000L)

            val response = generateResponse(text)
            _messages.update { it + response }
            _isTyping.update { false }
        }
    }

    private fun generateResponse(query: String): ChatMessage {
        val lower = query.lowercase()
        val botId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        return when {
            lower.contains("condition") || lower.contains("status") -> buildStatusResponse(botId, now)
            lower.contains("crop") || lower.contains("plant") -> buildCropResponse(botId, now)
            lower.contains("soil") || lower.contains("moisture") -> buildSoilResponse(botId, now)
            lower.contains("temperature") || lower.contains("temp") -> buildTemperatureResponse(botId, now)
            lower.contains("growth") || lower.contains("grow") || lower.contains("tip") -> buildGrowthResponse(botId, now)
            lower.contains("system") || lower.contains("device") -> buildSystemResponse(botId, now)
            lower.contains("help") || lower.contains("what can you do") -> buildHelpResponse(botId, now)
            else -> buildDefaultResponse(botId, now)
        }
    }

    private fun buildStatusResponse(id: String, timestamp: Long): ChatMessage {
        val tempStatus = if (mockTemp in 20.0..30.0) "optimal" else "needs attention"
        val humidityStatus = if (mockHumidity in 50.0..75.0) "optimal" else "needs attention"
        val soilStatus = if (mockSoil in 40.0..70.0) "optimal" else "needs attention"

        return ChatMessage(
            id = id,
            text = "Current greenhouse conditions:\n\n" +
                    "Temperature: ${mockTemp}°C ($tempStatus)\n" +
                    "Humidity: ${mockHumidity}% ($humidityStatus)\n" +
                    "Soil Moisture: ${mockSoil}% ($soilStatus)\n" +
                    "Light Intensity: ${mockLight.toInt()} lux\n\n" +
                    "Overall, your greenhouse is in good condition.",
            sender = "bot",
            timestamp = timestamp,
            isAnalysis = true,
            recommendations = listOf(
                "Continue monitoring temperature levels",
                "Maintain current irrigation schedule",
                "Check light exposure during cloudy days"
            )
        )
    }

    private fun buildCropResponse(id: String, timestamp: Long): ChatMessage {
        return ChatMessage(
            id = id,
            text = "Based on current conditions (temp=${mockTemp}°C, humidity=${mockHumidity}%, light=${mockLight.toInt()} lux), " +
                    "these crops would thrive in your greenhouse:\n\n" +
                    "Tomatoes - Excellent match for current temperature\n" +
                    "Lettuce - Good humidity levels support leafy growth\n" +
                    "Basil - Light intensity is ideal for herbs\n" +
                    "Peppers - Temperature is within optimal range",
            sender = "bot",
            timestamp = timestamp,
            recommendations = listOf(
                "Plant tomatoes in rows with 18-inch spacing",
                "Harvest lettuce before bolting in warm weather",
                "Pinch basil tips regularly for bushier growth",
                "Use companion planting with basil and peppers"
            )
        )
    }

    private fun buildSoilResponse(id: String, timestamp: Long): ChatMessage {
        val status = when {
            mockSoil < 40 -> "dry and needs watering"
            mockSoil in 40.0..70.0 -> "at optimal levels"
            else -> "too wet, reduce watering"
        }

        return ChatMessage(
            id = id,
            text = "Soil Moisture Analysis:\n\n" +
                    "Current reading: ${mockSoil}%\n" +
                    "Status: $status\n\n" +
                    "The ideal soil moisture range is 40-70% for most greenhouse crops. " +
                    "Your current level is ${if (mockSoil in 40.0..70.0) "within" else "outside"} the optimal range.",
            sender = "bot",
            timestamp = timestamp,
            isAnalysis = true,
            recommendations = listOf(
                if (mockSoil < 40) "Increase irrigation frequency" else if (mockSoil > 70) "Reduce irrigation frequency" else "Maintain current irrigation schedule",
                "Water early morning for best absorption",
                "Use mulch to retain soil moisture",
                "Check drainage to prevent waterlogging"
            )
        )
    }

    private fun buildTemperatureResponse(id: String, timestamp: Long): ChatMessage {
        val tempStatus = when {
            mockTemp < 18 -> "below optimal range"
            mockTemp in 18.0..28.0 -> "within optimal range"
            else -> "above optimal range"
        }

        return ChatMessage(
            id = id,
            text = "Temperature Report:\n\n" +
                    "Current: ${mockTemp}°C\n" +
                    "Status: $tempStatus\n\n" +
                    "Most greenhouse vegetables thrive between 18-28°C. " +
                    "Night temperatures can safely drop to 15-18°C.",
            sender = "bot",
            timestamp = timestamp,
            isAnalysis = true,
            recommendations = listOf(
                if (mockTemp > 28) "Open vents to reduce temperature" else if (mockTemp < 18) "Close vents and consider heating" else "Temperature is ideal, maintain current ventilation",
                "Monitor for sudden temperature fluctuations",
                "Use shade cloth during peak sun hours"
            )
        )
    }

    private fun buildGrowthResponse(id: String, timestamp: Long): ChatMessage {
        return ChatMessage(
            id = id,
            text = "Growth Optimization Tips:\n\n" +
                    "Based on your current sensor data, here are recommendations for maximizing crop growth:\n\n" +
                    "Light: Your ${mockLight.toInt()} lux reading is good for vegetative growth.\n" +
                    "Temperature: ${mockTemp}°C supports steady growth rates.\n" +
                    "Humidity: ${mockHumidity}% is ideal for transpiration.",
            sender = "bot",
            timestamp = timestamp,
            recommendations = listOf(
                "Prune lower leaves to improve air circulation",
                "Use trellising for vining crops like tomatoes",
                "Apply balanced fertilizer every 2 weeks",
                "Maintain consistent watering schedule",
                "Monitor for pests weekly"
            )
        )
    }

    private fun buildSystemResponse(id: String, timestamp: Long): ChatMessage {
        return ChatMessage(
            id = id,
            text = "System Status:\n\n" +
                    "All sensors are operational and transmitting data.\n\n" +
                    "Sensor Readings:\n" +
                    "Temperature Sensor: Active (${mockTemp}°C)\n" +
                    "Humidity Sensor: Active (${mockHumidity}%)\n" +
                    "Soil Moisture Sensor: Active (${mockSoil}%)\n" +
                    "Light Sensor: Active (${mockLight.toInt()} lux)\n\n" +
                    "Irrigation System: Standby\n" +
                    "Ventilation: Active",
            sender = "bot",
            timestamp = timestamp,
            isAnalysis = true,
            recommendations = listOf(
                "Calibrate soil moisture sensor monthly",
                "Clean light sensor lens quarterly",
                "Check irrigation lines for blockages",
                "Update firmware when available"
            )
        )
    }

    private fun buildHelpResponse(id: String, timestamp: Long): ChatMessage {
        return ChatMessage(
            id = id,
            text = "I can help you with:\n\n" +
                    "• System Status - Check all sensor readings and device status\n" +
                    "• Crop Advice - Get recommendations on what to grow\n" +
                    "• Soil Analysis - Monitor and optimize soil moisture\n" +
                    "• Temperature - Track and manage greenhouse temperature\n" +
                    "• Growth Tips - Learn how to maximize your yield\n" +
                    "• System Health - Verify all devices are working properly\n\n" +
                    "Try asking about any of these topics!",
            sender = "bot",
            timestamp = timestamp
        )
    }

    private fun buildDefaultResponse(id: String, timestamp: Long): ChatMessage {
        return ChatMessage(
            id = id,
            text = "I'm not sure I understand that question. I can help with system status, crop advice, soil moisture, temperature monitoring, growth tips, and system health. Try asking about one of these topics!",
            sender = "bot",
            timestamp = timestamp
        )
    }
}
