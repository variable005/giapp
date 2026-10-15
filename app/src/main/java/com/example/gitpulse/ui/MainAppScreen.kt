package com.example.gitpulse.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
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

    // Hierarchical Android Back Navigation Mapping
    BackHandler(enabled = showSettingsScreen) {
        showSettingsScreen = false
    }
    BackHandler(enabled = !showSettingsScreen && showNotificationsSheet) {
        showNotificationsSheet = false
    }
    BackHandler(enabled = !showSettingsScreen && !showNotificationsSheet && showSearchDialog) {
        showSearchDialog = false
    }
    BackHandler(enabled = !showSettingsScreen && !showNotificationsSheet && !showSearchDialog && currentTab != NavigationTab.HOME) {
        currentTab = NavigationTab.HOME
    }

    GitPulseTheme(
        darkTheme = isDark,
        dynamicColor = dynamicColor,
        amoled = amoledMode
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Container with Animated Settings Transition (Material 3 Emphasized Motion)
            AnimatedContent(
                targetState = showSettingsScreen,
                transitionSpec = {
                    if (targetState) {
                        (slideInVertically(
                            animationSpec = tween(320, easing = MotionTokens.EmphasizedDecelerateEasing)
                        ) { it / 6 } + fadeIn(animationSpec = tween(240, easing = LinearOutSlowInEasing))) togetherWith
                        (slideOutVertically(
                            animationSpec = tween(250, easing = MotionTokens.EmphasizedAccelerateEasing)
                        ) { -it / 10 } + fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing)))
                    } else {
                        (slideInVertically(
                            animationSpec = tween(280, easing = MotionTokens.EmphasizedDecelerateEasing)
                        ) { -it / 10 } + fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing))) togetherWith
                        (slideOutVertically(
                            animationSpec = tween(280, easing = MotionTokens.EmphasizedAccelerateEasing)
                        ) { it / 6 } + fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing)))
                    }
                },
                label = "settings_transition",
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) { inSettings ->
                if (inSettings) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { showSettingsScreen = false },
                        onOpenNotifications = { showNotificationsSheet = true }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Main Content Area with Seamless Modern Crossfade (Zero White Flash)
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(150, easing = LinearEasing))) togetherWith
                                (fadeOut(animationSpec = tween(150, easing = LinearEasing)))
                            },
                            label = "tab_crossfade",
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
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

                        // Floating Top Controls Overlay (STRICTLY NO APP BAR - Mathematically Symmetrical)
                        FloatingTopActions(
                            currentUsername = currentUsername,
                            onSearchClick = { showSearchDialog = true },
                            onSettingsClick = { showSettingsScreen = true },
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
