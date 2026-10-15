package com.example.gitpulse.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import com.example.gitpulse.ui.animation.MotionTokens
import com.example.gitpulse.ui.components.*
import com.example.gitpulse.ui.screens.*
import com.example.gitpulse.ui.viewmodel.AppThemeMode
import com.example.gitpulse.ui.viewmodel.GitPulseUiState
import com.example.gitpulse.ui.viewmodel.GitPulseViewModel
import kotlinx.coroutines.launch

@Composable
fun MainAppScreen(
    viewModel: GitPulseViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val amoledMode by viewModel.amoledPureBlack.collectAsState()

    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val currentUsername by viewModel.currentUsername.collectAsState()
    val personalToken by viewModel.personalToken.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    GitPulseTheme(
        darkTheme = isDark,
        dynamicColor = dynamicColor,
        amoled = amoledMode
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Container with Animated Settings Transition (M3 Expressive Springs)
            AnimatedContent(
                targetState = showSettingsScreen,
                transitionSpec = {
                    if (targetState) {
                        (slideInVertically(
                            animationSpec = MotionTokens.springBouncy()
                        ) { it / 4 } + fadeIn(MotionTokens.springGentle())) togetherWith
                        (slideOutVertically(
                            animationSpec = MotionTokens.springBouncy()
                        ) { -it / 8 } + fadeOut(MotionTokens.springGentle()))
                    } else {
                        (slideInVertically(
                            animationSpec = MotionTokens.springBouncy()
                        ) { -it / 8 } + fadeIn(MotionTokens.springGentle())) togetherWith
                        (slideOutVertically(
                            animationSpec = MotionTokens.springBouncy()
                        ) { it / 4 } + fadeOut(MotionTokens.springGentle()))
                    }
                },
                label = "settings_transition",
                modifier = Modifier.fillMaxSize()
            ) { inSettings ->
                if (inSettings) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { showSettingsScreen = false }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Main Content Area with M3 Expressive Shared Axis Spring Transitions
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                val direction = targetState.ordinal > initialState.ordinal
                                if (direction) {
                                    (slideInHorizontally(
                                        animationSpec = MotionTokens.springBouncy()
                                    ) { it / 3 } + fadeIn(MotionTokens.springGentle())) togetherWith
                                    (slideOutHorizontally(
                                        animationSpec = MotionTokens.springBouncy()
                                    ) { -it / 3 } + fadeOut(MotionTokens.springGentle()))
                                } else {
                                    (slideInHorizontally(
                                        animationSpec = MotionTokens.springBouncy()
                                    ) { -it / 3 } + fadeIn(MotionTokens.springGentle())) togetherWith
                                    (slideOutHorizontally(
                                        animationSpec = MotionTokens.springBouncy()
                                    ) { it / 3 } + fadeOut(MotionTokens.springGentle()))
                                }
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
                                        uiState = uiState,
                                        onRetry = { viewModel.refresh() }
                                    )
                                }
                                NavigationTab.ANALYTICS -> {
                                    AnalyticsScreen(
                                        uiState = uiState,
                                        onRetry = { viewModel.refresh() },
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

                        // Floating Top Controls Overlay (STRICTLY NO APP BAR - Perfectly Symmetrical)
                        FloatingTopActions(
                            currentUsername = currentUsername,
                            isDarkTheme = isDark,
                            onToggleTheme = {
                                val nextMode = when (themeMode) {
                                    AppThemeMode.SYSTEM -> if (systemInDark) AppThemeMode.LIGHT else AppThemeMode.DARK
                                    AppThemeMode.DARK -> AppThemeMode.LIGHT
                                    AppThemeMode.LIGHT -> AppThemeMode.DARK
                                }
                                viewModel.setThemeMode(nextMode)
                            },
                            onSearchClick = { showSearchDialog = true },
                            onNotificationsClick = { showNotificationsSheet = true },
                            onSettingsClick = {
                                showSettingsScreen = true
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
                    }
                }
            }

            // Global Snackbar Host
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (showSettingsScreen) 16.dp else 90.dp)
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
                        viewModel.addAccount(user, token)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Fetching real GitHub data for @$user...")
                        }
                    }
                )
            }
        }
    }
}
