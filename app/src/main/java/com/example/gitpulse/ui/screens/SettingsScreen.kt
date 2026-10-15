package com.example.gitpulse.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gitpulse.theme.*
import com.example.gitpulse.ui.animation.MotionTokens
import com.example.gitpulse.ui.viewmodel.AppThemeMode
import com.example.gitpulse.ui.viewmodel.GitPulseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GitPulseViewModel,
    onNavigateBack: () -> Unit,
    onOpenNotifications: () -> Unit = {},
    onNavigateToProjects: () -> Unit = {},
    onNavigateToProductReadme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val currentUsername by viewModel.currentUsername.collectAsState()
    val personalToken by viewModel.personalToken.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val amoledMode by viewModel.amoledPureBlack.collectAsState()
    val highRefreshAnim by viewModel.highRefreshAnimations.collectAsState()
    val dailyGoal by viewModel.dailyCommitGoal.collectAsState()
    val targetDays by viewModel.targetDaysPerWeek.collectAsState()
    val allowWeekendFreeze by viewModel.allowWeekendFreeze.collectAsState()
    val dailyPushWarning by viewModel.dailyPushWarningEnabled.collectAsState()
    val cutoffTime by viewModel.alertCutoffTime.collectAsState()
    val includePrivate by viewModel.includePrivateContributions.collectAsState()
    val maskSensitive by viewModel.maskSensitiveRepositories.collectAsState()
    val anonymizeCommits by viewModel.anonymizeCommitMessages.collectAsState()
    val cacheRetention by viewModel.cacheRetentionHours.collectAsState()
    val savedAccounts by viewModel.savedAccounts.collectAsState()

    var usernameInput by remember { mutableStateOf(currentUsername) }
    var tokenInput by remember { mutableStateOf(personalToken ?: "") }
    var isTokenVisible by remember { mutableStateOf(false) }

    var showPurgeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showSaveSnackbar by remember { mutableStateOf(false) }

    val cutoffOptions = listOf("07:00 PM", "08:00 PM", "08:30 PM", "09:30 PM", "11:00 PM")
    val cacheOptions = listOf(1, 6, 12, 24)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = 56.dp,
            bottom = 128.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Navigation Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateBack()
                    },
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate Back"
                    )
                }

                Column {
                    Text(
                        text = "Preferences",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Configure accounts, theme, streak engine, & cache",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. GitHub Account & Authentication Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GitHub Account",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    if (personalToken.isNullOrBlank()) "60 req/hr (Public)" else "5000 req/hr (Authenticated)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (personalToken.isNullOrBlank()) Icons.Default.Info else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (personalToken.isNullOrBlank()) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }

                    // Saved Accounts Quick Switcher Chips
                    Text(
                        text = "Quick Switch Account",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        savedAccounts.forEach { acc ->
                            val isSelected = acc.username.equals(currentUsername, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    usernameInput = acc.username
                                    viewModel.loadUserData(acc.username, acc.token)
                                },
                                label = {
                                    Text(
                                        text = "@${acc.username}",
                                        maxLines = 1,
                                        softWrap = false,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("GitHub Username") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Personal Access Token (PAT)") },
                        placeholder = { Text("ghp_... (read:user, repo)") },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                Icon(
                                    imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle token visibility"
                                )
                            }
                        },
                        visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.addAccount(usernameInput.trim(), tokenInput.trim().ifBlank { null })
                            showSaveSnackbar = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Synchronize", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // 3. Projects & Documentation Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Projects & Documentation",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Browse repository READMEs or review the complete technical product documentation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToProjects()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Browse Projects & READMEs", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToProductReadme()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View GitPulse Product README", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // 4. Material 3 Appearance & Theming Card (Theme toggle strictly resides here)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Material 3 Appearance",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Theme Mode Selector
                    Text(
                        text = "Theme Palette",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AppThemeMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = themeMode == mode,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setThemeMode(mode)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = AppThemeMode.entries.size)
                            ) {
                                Text(mode.title, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Dynamic Color (Material You)
                    ListItem(
                        headlineContent = { Text("Material You Dynamic Color") },
                        supportingContent = { Text("Derive palette dynamically from system wallpaper (Android 12+)") },
                        leadingContent = {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked = dynamicColor,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setDynamicColor(it)
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // AMOLED Pure Black
                    ListItem(
                        headlineContent = { Text("AMOLED Pure Black") },
                        supportingContent = { Text("Use pitch black #000000 surfaces for OLED battery savings") },
                        leadingContent = {
                            Icon(Icons.Default.Contrast, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked = amoledMode,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setAmoledPureBlack(it)
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // High Framerate Animations
                    ListItem(
                        headlineContent = { Text("Expressive Motion & Physics") },
                        supportingContent = { Text("Enable M3 spring curves, rolling counters, and micro-interactions") },
                        leadingContent = {
                            Icon(Icons.Default.Animation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked = highRefreshAnim,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setHighRefreshAnimations(it)
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }

        // 4. Telemetry, API Quota & Storage Graphs Card (NEW VISUAL ANALYTICS)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Telemetry & Storage Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Live Engine",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Graph 1: 7-Day API Request Burn Chart
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "7-Day API Call Volume",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (personalToken.isNullOrBlank()) "18 / 60 req (Public)" else "342 / 5000 req (PAT)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        ApiUsageBarChart(
                            dataPoints = listOf(
                                "M" to 14,
                                "T" to 28,
                                "W" to 20,
                                "T" to 42,
                                "F" to 35,
                                "S" to 12,
                                "S" to 24
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Graph 2: Local Cache & Storage Memory Allocation
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cache Allocation (6.2 MB)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "TTL: ${cacheRetention}h",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        CacheStorageBreakdownBar(
                            segments = listOf(
                                Triple("Profiles", 1.2f, MaterialTheme.colorScheme.primary),
                                Triple("Events", 3.4f, MaterialTheme.colorScheme.tertiary),
                                Triple("Languages", 0.9f, MaterialTheme.colorScheme.secondary),
                                Triple("Bitmaps", 0.7f, MaterialTheme.colorScheme.outline)
                            ),
                            totalSizeMb = 6.2f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Graph 3: API Round-Trip Sync Latency Sparkline
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sync Response Latency",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Avg 168ms • 99.9% Uptime",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        SyncLatencySparkline(
                            latenciesMs = listOf(195, 172, 158, 210, 162, 145, 150),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 5. Activity Notifications & Alerts Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Notifications & Activity Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ListItem(
                        headlineContent = { Text("Daily Cutoff Warning Alert") },
                        supportingContent = { Text("Push reminder if zero contributions are logged before cutoff") },
                        leadingContent = {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked = dailyPushWarning,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setDailyPushWarningEnabled(it)
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    // Cutoff Time Selector
                    Text(
                        text = "Alert Cutoff Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cutoffOptions.forEach { time ->
                            FilterChip(
                                selected = cutoffTime == time,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setAlertCutoffTime(time)
                                },
                                label = { Text(time, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenNotifications()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Activity Notifications Feed")
                    }
                }
            }
        }

        // 6. Streak Goals & Commit Targets
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Streak & Targets",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Daily Commit Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Commit Target",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("$dailyGoal commits / day") }
                        )
                    }

                    Slider(
                        value = dailyGoal.toFloat(),
                        onValueChange = { viewModel.setDailyCommitGoal(it.toInt()) },
                        valueRange = 1f..15f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Weekly Target Days
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Days Per Week",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val dayOptions = listOf(5, 6, 7)
                        SingleChoiceSegmentedButtonRow {
                            dayOptions.forEachIndexed { index, days ->
                                SegmentedButton(
                                    selected = targetDays == days,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setTargetDaysPerWeek(days)
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = dayOptions.size)
                                ) {
                                    Text("${days}d", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Weekend Freeze
                    ListItem(
                        headlineContent = { Text("Allow Weekend Freeze") },
                        supportingContent = { Text("Prevent streak reset if no commits are pushed on Saturday or Sunday") },
                        trailingContent = {
                            Switch(
                                checked = allowWeekendFreeze,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setAllowWeekendFreeze(it)
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }

        // 7. Privacy & Diagnostics
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Privacy & Diagnostics",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ListItem(
                        headlineContent = { Text("Include Private Contributions") },
                        supportingContent = { Text("Aggregate commits from private organizational repositories") },
                        trailingContent = {
                            Switch(
                                checked = includePrivate,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setIncludePrivateContributions(it)
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    ListItem(
                        headlineContent = { Text("Mask Sensitive Repositories") },
                        supportingContent = { Text("Anonymize proprietary repository names in activity feeds") },
                        trailingContent = {
                            Switch(
                                checked = maskSensitive,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setMaskSensitiveRepositories(it)
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    ListItem(
                        headlineContent = { Text("Anonymize Commit Messages") },
                        supportingContent = { Text("Hide message text in activity stream previews") },
                        trailingContent = {
                            Switch(
                                checked = anonymizeCommits,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setAnonymizeCommitMessages(it)
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }

        // 8. Cache, Storage & Data Management
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Cache & Data Management",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Cache Retention Hours
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cache TTL (Time To Live)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        SingleChoiceSegmentedButtonRow {
                            cacheOptions.forEachIndexed { index, hrs ->
                                SegmentedButton(
                                    selected = cacheRetention == hrs,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setCacheRetentionHours(hrs)
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = cacheOptions.size)
                                ) {
                                    Text("${hrs}h", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.refresh()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Force Synchronize Live Data")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showPurgeDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Purge Cache", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showResetDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Defaults", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }
                }
            }
        }

        // 9. App Info & Diagnostic Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "GitPulse v2.5.0 (Material 3 Expressive)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Engineered for @$currentUsername",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // Confirmation Dialogs
    if (showPurgeDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeDialog = false },
            title = { Text("Purge Cache?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("This removes cached GitHub profiles, event history, and language distributions, reloading fresh data directly from the GitHub API.") },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showPurgeDialog = false
                        viewModel.purgeCacheAndReload()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Purge & Reload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Preferences?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("This will restore default streak goals, theme options, and notification cutoffs.") },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showResetDialog = false
                        viewModel.resetAllSettingsToDefaults()
                        usernameInput = "variable005"
                        tokenInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * 7-Day API Request Burn Chart rendered with Canvas.
 */
@Composable
private fun ApiUsageBarChart(
    dataPoints: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val maxVal = (dataPoints.maxOfOrNull { it.second } ?: 1).coerceAtLeast(10)
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            val barWidth = 20.dp.toPx()
            val availableWidth = size.width
            val step = availableWidth / dataPoints.size
            val maxBarHeight = size.height - 16.dp.toPx()

            // Baseline
            drawLine(
                color = trackColor,
                start = Offset(0f, maxBarHeight),
                end = Offset(size.width, maxBarHeight),
                strokeWidth = 2.dp.toPx()
            )

            dataPoints.forEachIndexed { index, pair ->
                val xCenter = step * index + (step / 2f)
                val barHeight = ((pair.second.toFloat() / maxVal.toFloat()) * maxBarHeight).coerceAtLeast(8.dp.toPx())
                val topY = maxBarHeight - barHeight

                // Background slot
                drawRoundRect(
                    color = trackColor.copy(alpha = 0.5f),
                    topLeft = Offset(xCenter - barWidth / 2f, 0f),
                    size = Size(barWidth, maxBarHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Active bar
                val isPeak = pair.second == (dataPoints.maxOfOrNull { it.second } ?: 0)
                drawRoundRect(
                    color = if (isPeak) primaryColor else primaryColor.copy(alpha = 0.65f),
                    topLeft = Offset(xCenter - barWidth / 2f, topY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-axis day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dataPoints.forEach { (day, count) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Cache and Storage Allocation Stacked Bar with Symmetrical Legends.
 */
@Composable
private fun CacheStorageBreakdownBar(
    segments: List<Triple<String, Float, Color>>,
    totalSizeMb: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
        ) {
            segments.forEach { (_, sizeMb, color) ->
                val weight = (sizeMb / totalSizeMb).coerceAtLeast(0.01f)
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }

        // Two Symmetrical Rows of Legends
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem(label = "Profiles", value = "1.2 MB", color = MaterialTheme.colorScheme.primary)
            LegendItem(label = "Events", value = "3.4 MB", color = MaterialTheme.colorScheme.tertiary)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem(label = "Languages", value = "920 KB", color = MaterialTheme.colorScheme.secondary)
            LegendItem(label = "Bitmaps", value = "680 KB", color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun LegendItem(label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Sync Round-Trip Response Latency Sparkline with Canvas.
 */
@Composable
private fun SyncLatencySparkline(
    latenciesMs: List<Int>,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val container = MaterialTheme.colorScheme.surfaceContainerHighest
    val minVal = latenciesMs.minOrNull() ?: 100
    val maxVal = (latenciesMs.maxOfOrNull { it } ?: 300).coerceAtLeast(minVal + 1)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        val count = latenciesMs.size
        if (count < 2) return@Canvas
        val stepX = size.width / (count - 1)
        val h = size.height - 12.dp.toPx()

        val points = latenciesMs.mapIndexed { i, ms ->
            val norm = (ms - minVal).toFloat() / (maxVal - minVal).toFloat()
            val y = h - (norm * h) + 6.dp.toPx()
            Offset(i * stepX, y)
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = primary,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        points.forEach { p ->
            drawCircle(
                color = primary,
                radius = 3.5.dp.toPx(),
                center = p
            )
            drawCircle(
                color = container,
                radius = 1.5.dp.toPx(),
                center = p
            )
        }
    }
}
