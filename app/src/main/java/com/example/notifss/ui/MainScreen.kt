package com.example.notifss.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notifss.R
import com.example.notifss.Screen
import com.example.notifss.service.ContactItem
import com.example.notifss.service.NotificationItem
import com.example.notifss.service.NotificationService
import kotlinx.coroutines.flow.StateFlow

// Main content with tabs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    notificationsFlow: StateFlow<List<NotificationItem>>,
    onSendTestNotification: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Notifications,
        Screen.Test,
        Screen.About
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Circle, contentDescription = null) },
                        label = { Text(stringResource(when(screen) {
                            Screen.Notifications -> R.string.notifications_tab
                            Screen.Test -> R.string.test_tab
                            Screen.About -> R.string.about_tab
                        })) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Notifications.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Notifications.route) {
                NotificationsScreen(notificationsFlow)
            }
            composable(Screen.Test.route) {
                TestScreen(onSendTestNotification)
            }
            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}

// Notifications screen
@Composable
fun NotificationsScreen(notificationsFlow: StateFlow<List<NotificationItem>>) {
    val notifications by notificationsFlow.collectAsStateWithLifecycle()
    val contacts by NotificationService.contacts.observeAsState(emptyList())
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "contacts_list"
    ) {
        composable("contacts_list") {
            ContactsListScreen(contacts, navController)
        }
        composable(
            route = "message_screen/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            MessageScreen(navController, contactId)
        }
    }
}

// Contacts list screen
@Composable
fun ContactsListScreen(contacts: List<ContactItem>, navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (contacts.isEmpty()) {
            Text(
                text = stringResource(R.string.no_notifications),
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(contacts) { contact ->
                    ContactCard(contact) {
                        navController.navigate("message_screen/${contact.id}")
                    }
                }
            }
        }
    }
}

// Notification card (keeping for backward compatibility)
@Composable
fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // App info row with icon and name
            Row(verticalAlignment = Alignment.CenterVertically) {
                // App icon
                notification.appIcon?.let { drawable ->
                    Image(
                        painter = BitmapPainter(drawable.toBitmap().asImageBitmap()),
                        contentDescription = "App icon",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // App name
                Text(
                    text = notification.appName,
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Time
                Text(
                    text = notification.timeString,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = notification.content)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notification.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                
                // Delete button
                IconButton(
                    onClick = { NotificationService.deleteNotification(notification.id) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete notification",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Contact card for the contacts list
@Composable
fun ContactCard(contact: ContactItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Contact info row with app icon and name
            Row(verticalAlignment = Alignment.CenterVertically) {
                // App icon
                contact.appIcon?.let { drawable ->
                    Image(
                        painter = BitmapPainter(drawable.toBitmap().asImageBitmap()),
                        contentDescription = "App icon",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // Contact name
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Message count
                Text(
                    text = "${contact.messages.size} messages hfdsjkafhdsa",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Latest message preview (if any)
            if (contact.messages.isNotEmpty()) {
                val latestMessage = contact.messages.maxByOrNull { it.timestamp }
                latestMessage?.let { message ->
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Time of latest message
                    Text(
                        text = message.timeString,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.appName,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                
                // Delete button
                IconButton(
                    onClick = { NotificationService.deleteContact(contact.id) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete contact",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Test screen
@Composable
fun TestScreen(onSendTestNotification: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onSendTestNotification,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = stringResource(R.string.send_test_notification))
        }
    }
}

// About screen
@Composable
fun AboutScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.about_text),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .wrapContentSize(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
}