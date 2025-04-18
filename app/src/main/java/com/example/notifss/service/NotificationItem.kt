package com.example.notifss.service

import android.graphics.drawable.Drawable

/**
 * Represents a notification item
 */
data class NotificationItem(
    val id: String,
    val title: String,
    val content: String,
    val packageName: String,
    val timestamp: Long,
    val timeString: String,
    val appName: String,
    val appIcon: Drawable?
)