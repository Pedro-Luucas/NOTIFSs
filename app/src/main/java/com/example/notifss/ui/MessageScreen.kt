package com.example.notifss.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.notifss.service.ContactItem
import com.example.notifss.service.MessageItem

/**
 * Screen that displays messages from a specific contact in a chat-style interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(navController: NavController, contactId: String) {
    // Find the contact from the service
    val contacts = com.example.notifss.service.NotificationService.contacts.value ?: emptyList()
    val contact = contacts.find { it.id == contactId }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // App icon
                        contact?.appIcon?.let { drawable ->
                            Image(
                                painter = androidx.compose.ui.graphics.painter.BitmapPainter(drawable.toBitmap().asImageBitmap()),
                                contentDescription = "App icon",
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        // Contact name
                        Text(text = contact?.name ?: "Unknown Contact")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (contact == null) {
            // Contact not found
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                Text(
                    text = "Contact not found",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Display messages
            MessageList(
                contact = contact,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

/**
 * Displays a list of messages from a contact
 */
@Composable
fun MessageList(contact: ContactItem, modifier: Modifier = Modifier) {
    // Sort messages by timestamp (newest at the bottom)
    val sortedMessages = contact.messages.sortedBy { it.timestamp }
    
    if (sortedMessages.isEmpty()) {
        // No messages
        Box(modifier = modifier) {
            Text(
                text = "No messages",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Display messages in a chat-style list
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedMessages) { message ->
                MessageBubble(message = message, appName = contact.appName)
            }
        }
    }
}

/**
 * Displays a single message in a chat bubble
 */
@Composable
fun MessageBubble(message: MessageItem, appName: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Message bubble
        Box(
            modifier = Modifier
                .align(Alignment.Start)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        // Timestamp
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${message.timeString} • $appName",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}