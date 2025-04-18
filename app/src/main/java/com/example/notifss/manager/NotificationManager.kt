package com.example.notifss.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.notifss.R

/**
 * Manages notification-related operations including creating channels and sending test notifications
 */
class NotificationManager(private val context: Context) {
    
    // Channel ID for test notifications
    private val testChannelId = "test_channel"
    
    /**
     * Creates notification channel for test notifications
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Test Notifications"
            val descriptionText = "Channel for test notifications"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(testChannelId, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: android.app.NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Sends a test notification
     * @return true if notification was sent, false if permission is not granted
     */
    fun sendTestNotification(): Boolean {
        // Check if notification permission is granted
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }
        
        val notificationManager = NotificationManagerCompat.from(context)
        val builder = NotificationCompat.Builder(context, testChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.test_notification_title))
            .setContentText(context.getString(R.string.test_notification_content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
        
        try {
            notificationManager.notify(1, builder.build())
            return true
        } catch (e: SecurityException) {
            // This can happen on Android 13+ if POST_NOTIFICATIONS permission is not granted
            return false
        }
    }
}