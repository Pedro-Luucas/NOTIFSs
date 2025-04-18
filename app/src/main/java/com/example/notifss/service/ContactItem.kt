package com.example.notifss.service

import android.graphics.drawable.Drawable

/**
 * Represents a contact with their messages
 */
data class ContactItem(
    val id: String,          // Unique identifier for the contact (using title as key)
    val name: String,        // Contact name (from notification title)
    val packageName: String, // App package name
    val appName: String,     // App name
    val appIcon: Drawable?   // App icon
)

/**
 * Represents a message from a contact
 */
data class MessageItem(
    val id: String,          // Notification ID
    val content: String,     // Message content (from notification text)
    val timestamp: Long,     // Timestamp when the message was received
    val timeString: String   // Formatted time string
)