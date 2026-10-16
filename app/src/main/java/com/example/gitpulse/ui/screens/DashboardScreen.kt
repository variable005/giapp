package com.example.gitpulse.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gitpulse.data.GitActivityItem
import com.example.gitpulse.data.HeatmapDay
import com.example.gitpulse.data.remote.GitPulseAggregateData
import com.example.gitpulse.theme.*
import com.example.gitpulse.ui.animation.AnimatedNumberCounter
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.example.gitpulse.ui.animation.MotionTokens
import com.example.gitpulse.ui.companion.CompanionHero
import com.example.gitpulse.ui.viewmodel.GitPulseUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: GitPulseUiState,
    selectedYear: Int,
    availableYears: List<Int>,
    onYearSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToInspector: () -> Unit = {},
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
                LoadingDashboardSkeleton()
            }
            is GitPulseUiState.Error -> {
                ErrorDashboardState(
                    message = uiState.message,
                    onRetry = onRetry,
                    onSearchClick = onSearchClick
                )
            }
            is GitPulseUiState.Success -> {
                DashboardContent(
                    data = uiState.data,
                    selectedYear = selectedYear,
                    availableYears = availableYears,
                    onYearSelected = onYearSelected,
                    onSearchClick = onSearchClick,
                    onNavigateToInspector = onNavigateToInspector
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: GitPulseAggregateData,
    selectedYear: Int,
    availableYears: List<Int>,
    onYearSelected: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToInspector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val profile = data.profile

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = 108.dp,
            bottom = 156.dp,
            start = 0.dp,
            end = 0.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Kitsu companion hero: the fox lives on your contribution meadow.
        item {
            CompanionHero(data = data)
        }

        // 2. Profile metric tiles
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricTile(value = "${profile.reposCount}", label = "REPOS")
                MetricTile(value = profile.followersCount, label = "FOLLOWERS")
                MetricTile(value = "${profile.followingCount}", label = "FOLLOWING")
                MetricTile(value = profile.starsCount, label = "STARS", hasStar = true)
            }
        }

        // 3. Dual Active vs Peak Run Outlined Cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active Run Card
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE RUN",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        AnimatedNumberCounter(
                            count = profile.activeStreakDays,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            suffix = " Days"
                        )
                        Text(
                            text = "Live Active Status",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Peak Run Card
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ALL-TIME PEAK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        AnimatedNumberCounter(
                            count = profile.longestStreakDays,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            suffix = " Days"
                        )
                        Text(
                            text = "Record Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        // 4. Annual Contributions Section
        item {
            var selectedDay by remember(data.contributions) {
                mutableStateOf<HeatmapDay?>(
                    data.contributions.lastOrNull { it.count > 0 } ?: data.contributions.lastOrNull()
                )
            }

            val dayMap = remember(data.contributions) {
                val map = mutableMapOf<Pair<Int, Int>, HeatmapDay>()
                data.contributions.forEach { day ->
                    if (day.weekOfYear >= 0 && day.dayOfWeek in 0..6) {
                        map[Pair(day.weekOfYear, day.dayOfWeek)] = day
                    }
                }
                map
            }

            val totalWeeks = remember(data.contributions, dayMap) {
                val maxW = dayMap.keys.maxOfOrNull { it.first } ?: -1
                maxOf(maxW + 1, 0)
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column {
                            Text(
                                text = "Contributions",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (data.contributions.isEmpty()) "No public contributions in $selectedYear" else "${profile.totalAnnualCommits} total in $selectedYear",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Year selector: equal-width segments spanning the card for perfect symmetry.
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            availableYears.forEachIndexed { index, year ->
                                SegmentedButton(
                                    selected = year == selectedYear,
                                    onClick = { onYearSelected(year) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = availableYears.size),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("$year", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    if (data.contributions.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "No public contributions in $selectedYear",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "This profile has no public activity in this year. Public graphs load without a token — a token is only needed for private contributions.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FilledTonalButton(onClick = onSearchClick) {
                                    Text("Switch user")
                                }
                            }
                        }
                    } else {
                    // Heatmap Component: Fixed Weekday column + Horizontally Scrollable matrix
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Weekday labels (fixed on left)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(top = 18.dp)
                        ) {
                            val weekdayLabels = listOf("", "M", "", "W", "", "F", "")
                            weekdayLabels.forEach { label ->
                                Box(
                                    modifier = Modifier.size(width = 12.dp, height = 13.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (label.isNotEmpty()) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Horizontally scrollable 52-week matrix
                        val heatmapScrollState = rememberScrollState()
                        LaunchedEffect(data.contributions.size) {
                            heatmapScrollState.scrollTo(heatmapScrollState.maxValue)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(heatmapScrollState),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            // Month Labels Header Row
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                for (col in 0 until totalWeeks) {
                                    val dayInCol = (0..6).firstNotNullOfOrNull { r -> dayMap[Pair(col, r)] }
                                    val monthStr = dayInCol?.dateString?.takeIf { it.length >= 7 }?.substring(5, 7)
                                    val prevDayInCol = if (col > 0) (0..6).firstNotNullOfOrNull { r -> dayMap[Pair(col - 1, r)] } else null
                                    val prevMonthStr = prevDayInCol?.dateString?.takeIf { it.length >= 7 }?.substring(5, 7)
                                    val isNewMonth = col == 0 || (monthStr != null && monthStr != prevMonthStr)

                                    val monthLabel = if (isNewMonth && monthStr != null) {
                                        when (monthStr) {
                                            "01" -> "Jan"
                                            "02" -> "Feb"
                                            "03" -> "Mar"
                                            "04" -> "Apr"
                                            "05" -> "May"
                                            "06" -> "Jun"
                                            "07" -> "Jul"
                                            "08" -> "Aug"
                                            "09" -> "Sep"
                                            "10" -> "Oct"
                                            "11" -> "Nov"
                                            "12" -> "Dec"
                                            else -> ""
                                        }
                                    } else ""

                                    Box(
                                        modifier = Modifier.width(13.dp),
                                        contentAlignment = Alignment.BottomStart
                                    ) {
                                        if (monthLabel.isNotEmpty()) {
                                            Text(
                                                text = monthLabel,
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

                            // 7 Rows of Contribution Cells
                            for (row in 0..6) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    for (col in 0 until totalWeeks) {
                                        val day = dayMap[Pair(col, row)]
                                        val level = day?.level ?: 0
                                        val isSelected = selectedDay != null && day != null && selectedDay?.dateString == day.dateString

                                        val cellColor = when (level) {
                                            4 -> PrimaryEmerald
                                            3 -> PrimaryEmerald.copy(alpha = 0.82f)
                                            2 -> PrimaryEmerald.copy(alpha = 0.58f)
                                            1 -> PrimaryEmerald.copy(alpha = 0.32f)
                                            else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)
                                        }

                                        val scaleAnim by animateFloatAsState(
                                            targetValue = if (isSelected) 1.25f else 1.0f,
                                            animationSpec = MotionTokens.SpringBouncy,
                                            label = "cell_scale"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(13.dp)
                                                .scale(scaleAnim)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(cellColor)
                                                .then(
                                                    if (isSelected) {
                                                        Modifier.border(
                                                            width = 1.5.dp,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            shape = RoundedCornerShape(3.dp)
                                                        )
                                                    } else Modifier
                                                )
                                                .clickable(enabled = day != null) {
                                                    day?.let {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        selectedDay = it
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Selected Day Inspection Banner
                    AnimatedVisibility(
                        visible = selectedDay != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        selectedDay?.let { day ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(
                                                    when (day.level) {
                                                        4 -> PrimaryEmerald
                                                        3 -> PrimaryEmerald.copy(alpha = 0.82f)
                                                        2 -> PrimaryEmerald.copy(alpha = 0.58f)
                                                        1 -> PrimaryEmerald.copy(alpha = 0.32f)
                                                        else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                                    }
                                                )
                                        )
                                        Column {
                                            Text(
                                                text = day.dateString.ifEmpty { "Selected Day" },
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (day.count == 0) "No contributions recorded" else "${day.count} ${if (day.count == 1) "contribution" else "contributions"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (day.count > 0) PrimaryEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = onNavigateToInspector,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "Inspect Day",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Footer Legend & Instructions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scroll to review $selectedYear",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Less",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f), RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(10.dp).background(PrimaryEmerald.copy(alpha = 0.32f), RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(10.dp).background(PrimaryEmerald.copy(alpha = 0.58f), RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(10.dp).background(PrimaryEmerald.copy(alpha = 0.82f), RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(10.dp).background(PrimaryEmerald, RoundedCornerShape(2.dp)))
                            Text(
                                text = "More",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    } // end heatmap else
                }
            }
        }

        // 5. Recent Git Activity Stream Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity Stream",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onNavigateToInspector) {
                    Text("View All", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        items(data.recentActivities.take(5)) { item ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                RecentActivityCard(item)
            }
        }

        if (data.recentActivities.isEmpty()) {
            item {
                Text(
                    text = "No recent public activity found.",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoadingDashboardSkeleton(modifier: Modifier = Modifier) {
    val shimmerBrush = MotionTokens.rememberShimmerBrush()

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
                .height(300.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(shimmerBrush)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(shimmerBrush)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(shimmerBrush)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(shimmerBrush)
            )
        }
    }
}

@Composable
private fun ErrorDashboardState(
    message: String,
    onRetry: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "GitHub Connection Failed",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onSearchClick,
                        modifier = Modifier.weight(1f),
                        shape = CircleShape
                    ) {
                        Text("Change User")
                    }
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Retry", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.MetricTile(value: String, label: String, hasStar: Boolean = false) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (hasStar) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.sp
                ),
                maxLines = 1,
                softWrap = false,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricColumn(value: String, label: String, hasStar: Boolean = false) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (hasStar) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RecentActivityCard(item: GitActivityItem) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
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
                        text = item.repoName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                    if (item.prNumber != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = item.prNumber,
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (item.branch != null) {
                        Text(
                            text = item.branch,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (item.statusText != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
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
}
