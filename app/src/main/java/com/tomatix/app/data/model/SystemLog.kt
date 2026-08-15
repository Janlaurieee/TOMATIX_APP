package com.tomatix.app.data.model

data class SystemLog(
    val id: String = "",
    val time: String = "",
    val event: String = "",
    val type: String = "info"
)
