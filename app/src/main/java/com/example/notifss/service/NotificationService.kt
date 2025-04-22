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
        
        private val _contacts = MutableLiveData<List<ContactItem>>(emptyList())
        val contacts: LiveData<List<ContactItem>> = _contacts

        fun clearNotifications() {
            _contacts.postValue(emptyList())
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
        
        
        // Create message item for the contact
        val messageItem = MessageItem(
            id = sbn.key,
            content = text,
            timestamp = timestamp,
            timeString = timeString
        )
        
        if (contactExists(title)) {
            addMessageToContact(title, messageItem)
        } else {
            addContact(title, messageItem)
        }
        
    }
    




    private fun addContact(contactName: String, messageItem: MessageItem) {
        val currentContacts = _contacts.value ?: emptyList()
        
        // Create a unique ID for the contact based on the name
        val contactId = contactName.hashCode().toString()
        
        // Find if contact already exists
        val existingContactIndex = currentContacts.indexOfFirst { it.id == contactId }
        
        
            // Create new contact
            val newContact = ContactItem(
                id = contactId,
                name = contactName,
                messages = listOf(messageItem)
            )
            
            // Add to contacts list
            _contacts.postValue(currentContacts + newContact)
        }


    private fun contactExists(contactName: String): Boolean {
        val currentContacts = _contacts.value ?: emptyList()
        val contactId = contactName.hashCode().toString()
        return currentContacts.any { it.id == contactId }
    }



    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }



    private fun addMessageToContact(contactName: String, messageItem: MessageItem) {
        val currentContacts = _contacts.value ?: emptyList()
        val contactId = contactName.hashCode().toString()
        val contactIndex = currentContacts.indexOfFirst { it.id == contactId }
        if (contactIndex != -1) {
            val contact = currentContacts[contactIndex]
            val updatedMessages = contact.messages + messageItem
            val updatedContact = contact.copy(messages = updatedMessages)
            val updatedContacts = currentContacts.toMutableList()
            updatedContacts[contactIndex] = updatedContact
            _contacts.postValue(updatedContacts)
        }
    }
}