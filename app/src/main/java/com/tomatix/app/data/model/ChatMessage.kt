package com.tomatix.app.data.model

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val sender: String = "user",
    val timestamp: Long = System.currentTimeMillis(),
    val recommendations: List<String> = emptyList(),
    val isAnalysis: Boolean = false
)
