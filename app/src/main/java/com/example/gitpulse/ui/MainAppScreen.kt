package com.example.gitpulse.ui

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gitpulse.theme.GitPulseTheme
import com.example.gitpulse.ui.components.*
import com.example.gitpulse.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun MainAppScreen() {
    val systemInDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemInDark) }
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    GitPulseTheme(darkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Content Area with Animated Tab Transitions
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "tab_transition",
                modifier = Modifier.fillMaxSize()
            ) { targetTab ->
                when (targetTab) {
                    NavigationTab.HOME -> {
                        DashboardScreen(
                            onNavigateToInspector = {
                                currentTab = NavigationTab.INSPECTOR
                            }
                        )
                    }
                    NavigationTab.INSPECTOR -> {
                        DayInspectorScreen()
                    }
                    NavigationTab.ANALYTICS -> {
                        AnalyticsScreen(
                            onShareCard = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Telemetry card exported to gallery!")
                                }
                            }
                        )
                    }
                    NavigationTab.COMPARE -> {
                        WatchlistCompareScreen(
                            onAddAccountClick = {
                                showAddAccountDialog = true
                            }
                        )
                    }
                }
            }

            // Floating Top Actions: NO TOP APP BAR!
            // Clean floating buttons at the top for Notifications, Theme switch, and Account
            FloatingTopActions(
                isDarkTheme = isDarkTheme,
                onToggleTheme = { isDarkTheme = !isDarkTheme },
                onNotificationsClick = { showNotificationsSheet = true },
                onProfileClick = {
                    currentTab = NavigationTab.COMPARE
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Floating Bottom Navigation Bar (Detached Full Pill)
            FloatingBottomNav(
                currentTab = currentTab,
                onTabSelected = { selected ->
                    currentTab = selected
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Snackbar Host for export and purge feedback
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
            )

            // Notifications Bottom Sheet
            if (showNotificationsSheet) {
                NotificationsSheet(
                    onDismiss = { showNotificationsSheet = false }
                )
            }

            // Add Account Dialog
            if (showAddAccountDialog) {
                AddAccountDialog(
                    onDismiss = { showAddAccountDialog = false },
                    onAdd = { username, _ ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Account @$username added to watchlist!")
                        }
                    }
                )
            }
        }
    }
}
