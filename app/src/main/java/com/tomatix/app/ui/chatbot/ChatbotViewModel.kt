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
        return ChatMessage(
            id = id,
            text = "Live sensor data isn't connected yet.\n\n" +
                    "Once your greenhouse sensors are linked, I'll be able to show temperature, humidity, soil moisture, and incoming sunlight readings here.\n\n" +
                    "You can still ask me for general growing advice in the meantime.",
            sender = "bot",
            timestamp = timestamp,
            isAnalysis = true,
            recommendations = listOf(
                "Connect your sensor hub to enable live monitoring",
                "Keep sensors calibrated for accurate readings",
                "Check device connections regularly"
            )
        )
    }

    private fun buildCropResponse(id: String, timestamp: Long): ChatMessage {
        return ChatMessage(
            id = id,
            text = "Once your sensors are connected, I can recommend crops based on your current greenhouse conditions.\n\n" +
                    "In the meantime, these crops are commonly suited to greenhouse growing:\n\n" +
                    "Tomatoes - warm-season crop, great for greenhouses\n" +
                    "Lettuce - grows well in cooler greenhouse conditions\n" +
                    "Basil - thrives with consistent warmth and sunlight\n" +
                    "Peppers - do well in warm, humid environments",
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
        return ChatMessage(
            id = id,
            text = "Live soil moisture data isn't connected yet.\n\n" +
                    "The ideal soil moisture range is 40-70% for most greenhouse crops. Once your moisture sensor is linked, I can analyze your current reading and recommend watering adjustments.",
            sender = "bot",
            timestamp = timestamp,
            isAnalysis = true,
            recommendations = listOf(
                "Water early morning for best absorption",
                "Use mulch to retain soil moisture",
                "Check drainage to prevent waterlogging",
                "Calibrate the moisture sensor after installation"
            )
        )
    }

    private fun buildTemperatureResponse(id: String, timestamp: Long): ChatMessage {
        return ChatMessage(
            id = id,
            text = "Live temperature data isn't connected yet.\n\n" +
                    "Most greenhouse vegetables thrive between 18-28°C. Once your temperature sensor is linked, I can compare your current reading against this optimal range and suggest adjustments.",
            sender = "bot",
            timestamp = timestamp,
            isAnalysis = true,
            recommendations = listOf(
                "Maintain good ventilation to avoid overheating",
                "Monitor for sudden temperature fluctuations",
                "Use shade cloth during peak sun hours"
            )
        )
    }

    private fun buildGrowthResponse(id: String, timestamp: Long): ChatMessage {
        return ChatMessage(
            id = id,
            text = "Growth Optimization Tips:\n\n" +
                    "Once your sensors are connected, I can tailor these tips to your greenhouse's actual conditions.\n\n" +
                    "• Keep temperature between 18-28°C for most crops\n" +
                    "• Maintain soil moisture around 40-70%\n" +
                    "• Ensure good ventilation for healthy transpiration",
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
                    "Live device and sensor data isn't connected yet.\n\n" +
                    "Once connected, I'll show the status of your temperature, humidity, soil moisture, and incoming sunlight sensors, along with the irrigation and ventilation systems.",
            sender = "bot",
            timestamp = timestamp,
            isAnalysis = true,
            recommendations = listOf(
                "Calibrate soil moisture sensor after setup",
                "Clean the sunlight sensor lens periodically",
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
