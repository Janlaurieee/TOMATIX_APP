package com.tomatix.app.data.model

data class NotificationSettings(
    val email: Boolean = true,
    val push: Boolean = true,
    val sms: Boolean = false,
    val criticalOnly: Boolean = false
)
