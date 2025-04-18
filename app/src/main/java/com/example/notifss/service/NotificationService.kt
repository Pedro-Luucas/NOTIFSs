package com.example.notifss.service

import android.app.Notification
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationService : NotificationListenerService() {

    companion object {
        private val _notifications = MutableLiveData<List<NotificationItem>>(emptyList())
        val notifications: LiveData<List<NotificationItem>> = _notifications
        
        private val _contacts = MutableLiveData<List<ContactItem>>(emptyList())
        val contacts: LiveData<List<ContactItem>> = _contacts

        fun clearNotifications() {
            _notifications.postValue(emptyList())
            _contacts.postValue(emptyList())
        }
        
        fun deleteNotification(notificationId: String) {
            val currentList = _notifications.value ?: emptyList()
            val updatedList = currentList.filterNot { it.id == notificationId }
            _notifications.postValue(updatedList)
            
            // Also remove the message from contacts
            val currentContacts = _contacts.value ?: emptyList()
            val updatedContacts = currentContacts.map { contact ->
                val updatedMessages = contact.messages.filterNot { it.id == notificationId }
                if (updatedMessages.isEmpty()) {
                    null // Mark for removal if no messages left
                } else {
                    contact.copy(messages = updatedMessages)
                }
            }.filterNotNull()
            _contacts.postValue(updatedContacts)
        }
        
        fun deleteContact(contactId: String) {
            val currentContacts = _contacts.value ?: emptyList()
            val updatedContacts = currentContacts.filterNot { it.id == contactId }
            _contacts.postValue(updatedContacts)
            
            // Also remove all associated notifications
            val contactToRemove = currentContacts.find { it.id == contactId } ?: return
            val messageIds = contactToRemove.messages.map { it.id }
            val currentNotifications = _notifications.value ?: emptyList()
            val updatedNotifications = currentNotifications.filterNot { it.id in messageIds }
            _notifications.postValue(updatedNotifications)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        addNotification(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // We no longer remove notifications when they're dismissed from the system
        // They will only be removed when the user presses the delete button in the app
        // removeNotification(sbn)
    }

    private fun addNotification(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val packageName = sbn.packageName
        val timestamp = sbn.postTime
        
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeString = dateFormat.format(Date(timestamp))
        
        // Get app name and icon
        val packageManager = applicationContext.packageManager
        val appName = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName // Fallback to package name if app name can't be retrieved
        }
        
        val appIcon = try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null // Fallback to null if icon can't be retrieved
        }
        
        val notificationItem = NotificationItem(
            id = sbn.key,
            title = title + " titleNOTI",
            content = text + " contentNOTI",
            packageName = packageName + " packageNameNOTI",
            timestamp = timestamp,
            timeString = timeString + " timeStringNOTI",
            appName = appName + " appNameNOTI",
            appIcon = appIcon
        )
        
        // Add to notifications list
        val currentList = _notifications.value ?: emptyList()
        _notifications.postValue(currentList + notificationItem)
        
        // Create message item for the contact
        val messageItem = MessageItem(
            id = sbn.key,
            content = text,
            timestamp = timestamp,
            timeString = timeString
        )
        
        // Add or update contact
        addOrUpdateContact(title, messageItem, packageName, appName, appIcon)
    }
    
    private fun addOrUpdateContact(contactName: String, messageItem: MessageItem, packageName: String, appName: String, appIcon: Drawable?) {
        val currentContacts = _contacts.value ?: emptyList()
        
        // Create a unique ID for the contact based on the name
        val contactId = contactName.hashCode().toString()
        
        // Find if contact already exists
        val existingContactIndex = currentContacts.indexOfFirst { it.id == contactId }
        
        if (existingContactIndex >= 0) {
            // Update existing contact with new message
            val existingContact = currentContacts[existingContactIndex]
            val updatedMessages = existingContact.messages + messageItem
            val updatedContact = existingContact.copy(messages = updatedMessages)
            
            // Replace the contact in the list
            val updatedContacts = currentContacts.toMutableList()
            updatedContacts[existingContactIndex] = updatedContact
            _contacts.postValue(updatedContacts)
        } else {
            // Create new contact
            val newContact = ContactItem(
                id = contactId,
                name = contactName + " CONTATCT",
                packageName = packageName,
                appName = appName,
                appIcon = appIcon,
                messages = listOf(messageItem)
            )
            
            // Add to contacts list
            _contacts.postValue(currentContacts + newContact)
        }
    }

    private fun removeNotification(sbn: StatusBarNotification) {
        val currentList = _notifications.value ?: return
        val updatedList = currentList.filterNot { it.id == sbn.key }
        _notifications.postValue(updatedList)
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }
}