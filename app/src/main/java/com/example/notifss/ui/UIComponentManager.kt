package com.example.notifss.ui

import android.app.AlertDialog
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.example.notifss.PermissionScreen
import com.example.notifss.R
import com.example.notifss.manager.PermissionManager
import com.example.notifss.service.NotificationItem
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages UI components and screens for the application
 */
class UIComponentManager(private val context: Context) {
    
    /**
     * Shows a dialog to request notification permission
     * @param onGoToSettings Callback when user chooses to go to settings
     */
    fun showNotificationPermissionDialog(onGoToSettings: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder
            .setTitle(context.getString(R.string.notification_permission_title, "Notification Permission"))
            .setMessage(context.getString(R.string.notification_permission_message, "This app needs notification permission to send test notifications. Please enable it in settings."))
            .setPositiveButton(context.getString(R.string.go_to_settings)) { _, _ ->
                onGoToSettings()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }
    
    /**
     * Determines which screen to show based on permission status
     * @param hasPermission Current permission state
     * @param permissionManager Permission manager instance
     * @param notificationsFlow Flow of notification items
     * @param onSendTestNotification Callback for sending test notification
     * @return Composable function to render the appropriate screen
     */
    @Composable
    fun getScreenBasedOnPermission(
        hasPermission: MutableState<Boolean>,
        permissionManager: PermissionManager,
        notificationsFlow: StateFlow<List<NotificationItem>>,
        onSendTestNotification: () -> Unit
    ): @Composable () -> Unit {
        return if (hasPermission.value) {
            { MainScreen(notificationsFlow, onSendTestNotification) }
        } else {
            {
                PermissionScreen(
                    hasPermission = hasPermission.value,
                    onRequestPermission = { 
                        context.startActivity(permissionManager.createNotificationListenerSettingsIntent())
                    },
                    onNavigateToMain = { /* Not needed anymore */ }
                )
            }
        }
    }
}