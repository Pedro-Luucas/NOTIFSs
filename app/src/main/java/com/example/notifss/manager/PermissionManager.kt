package com.example.notifss.manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Manages permission-related operations for the application
 */
class PermissionManager(private val context: Context) {
    
    /**
     * Checks if notification listener permission is granted
     * @return true if permission is granted, false otherwise
     */
    fun isNotificationServiceEnabled(): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }
    
    /**
     * Checks if notification permission is granted (for Android 13+)
     * @return true if permission is granted or not needed (Android < 13), false otherwise
     */
    fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android < 13, this permission is not required at runtime
            true
        }
    }
    
    /**
     * Request notification permission for Android 13+
     * @param activity The activity to request permission from
     * @param launcher The permission launcher to use
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_CODE
            )
        }
    }
    
    /**
     * Creates an intent to open notification listener settings
     * @return Intent to open notification listener settings
     */
    fun createNotificationListenerSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }
    
    /**
     * Creates an intent to open notification settings for the app
     * @return Intent to open app notification settings
     */
    fun createNotificationSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                putExtra("app_package", context.packageName)
                putExtra("app_uid", context.applicationInfo.uid)
            }
        }
    }
    
    companion object {
        const val NOTIFICATION_PERMISSION_CODE = 100
    }
}