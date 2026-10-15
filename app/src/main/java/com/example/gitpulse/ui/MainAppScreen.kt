package com.example.gitpulse.ui

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gitpulse.theme.GitPulseTheme
import com.example.gitpulse.ui.components.*
import com.example.gitpulse.ui.screens.*
import com.example.gitpulse.ui.viewmodel.GitPulseViewModel
import kotlinx.coroutines.launch

@Composable
fun MainAppScreen(
    viewModel: GitPulseViewModel = viewModel()
) {
    val systemInDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemInDark) }
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val currentUsername by viewModel.currentUsername.collectAsState()
    val personalToken by viewModel.personalToken.collectAsState()

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
                            uiState = uiState,
                            onRetry = { viewModel.refresh() },
                            onSearchClick = { showSearchDialog = true },
                            onNavigateToInspector = {
                                currentTab = NavigationTab.INSPECTOR
                            }
                        )
                    }
                    NavigationTab.INSPECTOR -> {
                        DayInspectorScreen(
                            uiState = uiState
                        )
                    }
                    NavigationTab.ANALYTICS -> {
                        AnalyticsScreen(
                            uiState = uiState,
                            onShareCard = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Telemetry card exported to gallery!")
                                }
                            }
                        )
                    }
                    NavigationTab.COMPARE -> {
                        WatchlistCompareScreen(
                            currentUsername = currentUsername,
                            onSwitchUser = { username ->
                                viewModel.switchUser(username)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Switched active profile to @$username")
                                }
                            },
                            onAddAccountClick = {
                                showSearchDialog = true
                            },
                            onPurgeCache = {
                                viewModel.refresh()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Cache purged. Reloading live data.")
                                }
                            }
                        )
                    }
                }
            }

            // Floating Top Controls Overlay (STRICTLY NO APP BAR)
            FloatingTopActions(
                currentUsername = currentUsername,
                isDarkTheme = isDarkTheme,
                onToggleTheme = { isDarkTheme = !isDarkTheme },
                onSearchClick = { showSearchDialog = true },
                onNotificationsClick = { showNotificationsSheet = true },
                onRefreshClick = {
                    viewModel.refresh()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Refreshing GitHub stats for @$currentUsername...")
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Floating Bottom Navigation Pill (Detached Full Pill)
            FloatingBottomNav(
                currentTab = currentTab,
                onTabSelected = { selected ->
                    currentTab = selected
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Snackbar Host
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

            // Search / Add Account Dialog
            if (showSearchDialog) {
                SearchUserDialog(
                    initialUsername = currentUsername,
                    initialToken = personalToken,
                    onDismiss = { showSearchDialog = false },
                    onConfirm = { user, token ->
                        viewModel.loadUserData(user, token)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Fetching real GitHub data for @$user...")
                        }
                    }
                )
            }
        }
    }
}
