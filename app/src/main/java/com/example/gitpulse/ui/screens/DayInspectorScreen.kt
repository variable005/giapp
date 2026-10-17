package com.example.gitpulse.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gitpulse.data.GitActivityItem
import com.example.gitpulse.data.remote.GitPulseAggregateData
import com.example.gitpulse.theme.*
import com.example.gitpulse.ui.animation.AnimatedNumberCounter
import com.example.gitpulse.ui.animation.MotionTokens
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.example.gitpulse.ui.viewmodel.GitPulseUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MonthAbbrev = mapOf(
    "01" to "Jan", "02" to "Feb", "03" to "Mar", "04" to "Apr",
    "05" to "May", "06" to "Jun", "07" to "Jul", "08" to "Aug",
    "09" to "Sep", "10" to "Oct", "11" to "Nov", "12" to "Dec"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayInspectorScreen(
    uiState: GitPulseUiState,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isRefreshing = uiState is GitPulseUiState.Loading

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onRetry()
        },
        modifier = modifier.fillMaxSize()
    ) {
        when (uiState) {
            is GitPulseUiState.Loading -> {
                LoadingDayInspectorSkeleton()
            }
            is GitPulseUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(text = uiState.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }
            is GitPulseUiState.Success -> {
                DayInspectorContent(data = uiState.data)
            }
        }
    }
}

@Composable
private fun DayInspectorContent(
    data: GitPulseAggregateData,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableIntStateOf(15) }
    var includePrivate by remember { mutableStateOf(true) }
    var maskSensitive by remember { mutableStateOf(false) }

    // GitHub's year calendar can include future days (the fetch asks for the
    // whole Jan-Dec range), so a plain takeLast(28) lands on an empty December.
    // Anchor the rolling window to today, then take the most recent 28 days.
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val recentDays = data.contributions
        .filter { it.dateString <= today }
        .sortedBy { it.dateString }
        .takeLast(28)

    val selectedDate = recentDays.getOrNull(selectedDay - 1)?.dateString
    val selectedDateLabel = selectedDate
        ?.takeIf { it.length >= 10 }
        ?.let { "${MonthAbbrev[it.substring(5, 7)].orEmpty()} ${it.substring(8, 10).trimStart('0')}" }
        .orEmpty()
        .ifEmpty { "Day $selectedDay" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = 108.dp,
            bottom = 156.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Month Calendar Header Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
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
                        Column {
                            Text(
                                text = "Recent Cycle",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Active Rolling 4 Weeks",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "Active Sprint",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Month header: one label where a new month starts.
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (d in 0..6) {
                            val month = recentDays.getOrNull(d)?.dateString
                                ?.takeIf { it.length >= 7 }?.substring(5, 7)
                            val prevMonth = if (d > 0) {
                                recentDays.getOrNull(d - 1)?.dateString
                                    ?.takeIf { it.length >= 7 }?.substring(5, 7)
                            } else null
                            val label = if (month != null && (d == 0 || month != prevMonth)) {
                                MonthAbbrev[month].orEmpty()
                            } else ""
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (label.isNotEmpty()) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                            }
                        }
                    }

                    // Weekdays Row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 4 Weeks x 7 Days grid. Each cell is filled by its
                    // contribution level, reusing the dashboard heatmap ramp.
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (w in 0..3) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (d in 0..6) {
                                    val index = w * 7 + d
                                    val dayItem = recentDays.getOrNull(index)
                                    val dayNum = index + 1
                                    val isHighlighted = dayNum == selectedDay
                                    val level = dayItem?.level ?: 0
                                    val dayLabel = dayItem?.dateString
                                        ?.takeIf { it.length >= 10 }
                                        ?.substring(8, 10)
                                        ?.trimStart('0')
                                        .orEmpty()
                                        .ifEmpty { "$dayNum" }

                                    val cellColor = when (level) {
                                        4 -> PrimaryEmerald
                                        3 -> PrimaryEmerald.copy(alpha = 0.82f)
                                        2 -> PrimaryEmerald.copy(alpha = 0.58f)
                                        1 -> PrimaryEmerald.copy(alpha = 0.32f)
                                        else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)
                                    }
                                    val contentColor = if (level >= 3) {
                                        OnPrimaryEmerald
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }

                                    val scaleAnim by animateFloatAsState(
                                        targetValue = if (isHighlighted) 1.08f else 1.0f,
                                        animationSpec = MotionTokens.SpringBouncy,
                                        label = "cell_scale"
                                    )

                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        Surface(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .scale(scaleAnim)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { selectedDay = dayNum },
                                            shape = RoundedCornerShape(10.dp),
                                            color = cellColor,
                                            border = if (isHighlighted) {
                                                BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface)
                                            } else null
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = dayLabel,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = contentColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Activity Density Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Activity Density",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f), RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(10.dp).background(PrimaryEmerald.copy(alpha = 0.32f), RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(10.dp).background(PrimaryEmerald.copy(alpha = 0.58f), RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(10.dp).background(PrimaryEmerald.copy(alpha = 0.82f), RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(10.dp).background(PrimaryEmerald, RoundedCornerShape(2.dp)))
                            Text(text = "More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 2. Selected Day Hero Banner
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "$selectedDateLabel Overview",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Optimal git telemetry pace",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AnimatedNumberCounter(
                                count = (selectedDay * 3) % 8 + 1,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Actions\nLogged",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. Activity Stream Section Header
        item {
            Text(
                text = "Live Activity Stream",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(data.recentActivities) { act ->
            InspectorActivityItemCard(act, maskSensitive)
        }

        // 4. Data Visibility & Privacy Settings Card
        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Data Visibility & Privacy",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    ListItem(
                        headlineContent = { Text("Include Private Contributions") },
                        supportingContent = { Text("Render activity from private repositories") },
                        trailingContent = {
                            Switch(
                                checked = includePrivate,
                                onCheckedChange = { includePrivate = it }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    ListItem(
                        headlineContent = { Text("Mask Sensitive Repositories") },
                        supportingContent = { Text("Obfuscate client project names in screenshots") },
                        trailingContent = {
                            Switch(
                                checked = maskSensitive,
                                onCheckedChange = { maskSensitive = it }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun InspectorActivityItemCard(item: GitActivityItem, maskSensitive: Boolean) {
    val displayRepoName = if (maskSensitive) "confidential / masked-repo" else item.repoName

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.type) {
                                com.example.gitpulse.data.ActivityType.PULL_REQUEST -> Icons.AutoMirrored.Filled.CallMerge
                                com.example.gitpulse.data.ActivityType.REVIEW -> Icons.Default.RateReview
                                else -> Icons.Default.Commit
                            },
                            contentDescription = null,
                            tint = when (item.type) {
                                com.example.gitpulse.data.ActivityType.PULL_REQUEST -> SecondaryEmerald
                                com.example.gitpulse.data.ActivityType.REVIEW -> TertiaryAmber
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = displayRepoName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = item.timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.commitHash != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = item.commitHash,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (item.statusText != null) {
                    Text(
                        text = item.statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingDayInspectorSkeleton(modifier: Modifier = Modifier) {
    val shimmer = MotionTokens.rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 108.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(shimmer)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(shimmer)
        )
    }
}
