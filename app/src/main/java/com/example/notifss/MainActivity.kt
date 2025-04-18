package com.example.notifss

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import com.example.notifss.manager.NotificationManager
import com.example.notifss.manager.PermissionManager
import com.example.notifss.service.ContactItem
import com.example.notifss.service.NotificationItem
import com.example.notifss.service.NotificationService
import com.example.notifss.ui.ContactsListScreen
import com.example.notifss.ui.MainScreen
import com.example.notifss.ui.MessageScreen
import com.example.notifss.ui.UIComponentManager
import com.example.notifss.ui.theme.NOTIFSsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Define sealed class for navigation destinations
sealed class Screen(val route: String, val title: String) {
    object Notifications : Screen("notifications", "Notifications")
    object Test : Screen("test", "Test")
    object About : Screen("about", "About")
}

class MainActivity : ComponentActivity() {
    // Flow to hold notification items
    private val _notificationsFlow = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notificationsFlow: StateFlow<List<NotificationItem>> = _notificationsFlow.asStateFlow()
    
    // Manager instances
    private lateinit var notificationManager: NotificationManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var uiComponentManager: UIComponentManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize managers
        notificationManager = NotificationManager(this)
        permissionManager = PermissionManager(this)
        uiComponentManager = UIComponentManager(this)
        
        // Create notification channel
        notificationManager.createNotificationChannel()
        
        // Check and request notification permission for Android 13+
        if (!permissionManager.isNotificationPermissionGranted()) {
            permissionManager.requestNotificationPermission(this)
        }
        
        // Observe notifications from the service
        NotificationService.notifications.observe(this) { notifications ->
            _notificationsFlow.value = notifications
        }
        
        setContent {
            NOTIFSsTheme {
                val hasPermission = remember { mutableStateOf(permissionManager.isNotificationServiceEnabled()) }
                
                // Check permission on each composition
                LaunchedEffect(Unit) {
                    hasPermission.value = permissionManager.isNotificationServiceEnabled()
                }
                
                // Get the appropriate screen based on permission status
                val screenContent = uiComponentManager.getScreenBasedOnPermission(
                    hasPermission = hasPermission,
                    permissionManager = permissionManager,
                    notificationsFlow = notificationsFlow,
                    onSendTestNotification = ::sendTestNotification
                )
                
                // Display the screen
                screenContent()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Re-check permission status when app resumes
        setContent {
            NOTIFSsTheme {
                val hasPermission = remember { mutableStateOf(permissionManager.isNotificationServiceEnabled()) }
                
                // Force refresh of permission status
                LaunchedEffect(Unit) {
                    hasPermission.value = permissionManager.isNotificationServiceEnabled()
                }
                
                // Get the appropriate screen based on permission status
                val screenContent = uiComponentManager.getScreenBasedOnPermission(
                    hasPermission = hasPermission,
                    permissionManager = permissionManager,
                    notificationsFlow = notificationsFlow,
                    onSendTestNotification = ::sendTestNotification
                )
                
                // Display the screen
                screenContent()
            }
        }
    }
    
    // Function to send a test notification
    private fun sendTestNotification() {
        // Try to send notification, show dialog if permission not granted
        if (!notificationManager.sendTestNotification()) {
            uiComponentManager.showNotificationPermissionDialog {
                startActivity(permissionManager.createNotificationSettingsIntent())
            }
        }
    }
}

// Permission request screen
@Composable
fun PermissionScreen(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.permission_required),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.permission_explanation),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRequestPermission) {
            Text(text = stringResource(R.string.go_to_settings))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNavigateToMain,
            enabled = hasPermission
        ) {
            Text(text = "Go to Main Screen")
        }
    }
}

// Main content with tabs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NotificationsScreen(notificationsFlow: StateFlow<List<NotificationItem>>) {
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