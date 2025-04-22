package com.example.notifss.service

import android.graphics.drawable.Drawable

/**
 * Represents a contact with their messages
 */
data class ContactItem(
    val id: String,          // Unique identifier for the contact (using title hashcode as key)
    val name: String,        // Contact name (from notification title)// App icon
    val messages: List<MessageItem> = emptyList() // List of messages for this contact
)

data class MessageItem(
    val id: String,          // Unique identifier for the message (using notification key)
    val content: String,     // Message content (from notification text)
    val timestamp: Long,     // Timestamp of the message
    val timeString: String   // Formatted time string
)