package com.example.gitpulse.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gitpulse.data.GitActivityItem
import com.example.gitpulse.data.MockData
import com.example.gitpulse.theme.*

@Composable
fun DayInspectorScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Month Grid") }
    var selectedDay by remember { mutableIntStateOf(18) }
    var selectedFilter by remember { mutableStateOf("All") }
    var includePrivate by remember { mutableStateOf(true) }
    var maskSensitive by remember { mutableStateOf(false) }

    val daySummary = MockData.daySummary

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = 76.dp, // Space for floating top actions
            bottom = 100.dp, // Space for floating bottom nav
            start = 20.dp,
            end = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Month Calendar Header Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, OutlineBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Month & Active Sprint Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "December",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "2024",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Active Sprint",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous Month",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next Month",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // View Modes: Month Grid / Timeline / Audit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Month Grid", "Timeline", "Audit").forEach { tab ->
                            val isSelected = tab == selectedTab
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .clickable { selectedTab = tab },
                                shape = CircleShape,
                                color = if (isSelected) PrimaryEmerald else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = tab,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) OnPrimaryEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Weekdays Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Month Calendar Days Grid (4 weeks x 7 days)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val weeks = listOf(
                            listOf(1 to 1, 2 to 2, 3 to 4, 4 to 4, 5 to 2, 6 to 1, 7 to 0),
                            listOf(8 to 0, 9 to 2, 10 to 4, 11 to 4, 12 to 4, 13 to 2, 14 to 1),
                            listOf(15 to 0, 16 to 2, 17 to 4, 18 to 4, 19 to 2, 20 to 4, 21 to 0),
                            listOf(22 to 0, 23 to 2, 24 to 1, 25 to 0, 26 to 2, 27 to 4, 28 to 0)
                        )

                        weeks.forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                week.forEach { (dayNum, intensity) ->
                                    val isHighlighted = dayNum == selectedDay
                                    val dotColor = when (intensity) {
                                        4 -> PrimaryEmerald
                                        2 -> SecondaryContainerEmerald
                                        1 -> SurfaceContainerHighest
                                        else -> Color.Transparent
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { selectedDay = dayNum },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isHighlighted) SurfaceContainerHighest else SurfaceContainerLow,
                                        border = if (isHighlighted) BorderStroke(1.5.dp, PrimaryEmerald) else null
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isHighlighted) PrimaryEmerald else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(dotColor, CircleShape)
                                            )
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
                            Text(
                                text = "Less",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(modifier = Modifier.size(10.dp).background(SurfaceContainerHighest, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(10.dp).background(SecondaryContainerEmerald, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(10.dp).background(PrimaryContainerEmerald, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(10.dp).background(PrimaryEmerald, RoundedCornerShape(2.dp)))
                            Text(
                                text = "More",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Selected Day Hero Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, OutlineBorder)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = PrimaryEmerald,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = daySummary.dateDisplay,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = daySummary.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${daySummary.totalActions}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = PrimaryEmerald
                        )
                        Text(
                            text = "Total Actions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Contribution Split Cards & Segmented Progress Bar
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, OutlineBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Contribution Split",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Selected Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Multi-Segmented Progress Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLow),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(modifier = Modifier.weight(0.68f).fillMaxHeight().background(PrimaryEmerald))
                        Box(modifier = Modifier.weight(0.18f).fillMaxHeight().background(SecondaryEmerald))
                        Box(modifier = Modifier.weight(0.10f).fillMaxHeight().background(TertiaryAmber))
                        Box(modifier = Modifier.weight(0.04f).fillMaxHeight().background(SurfaceContainerHighest))
                    }

                    // 4 Split Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SplitMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Commit,
                            iconTint = PrimaryEmerald,
                            title = "${daySummary.commitsCount} Commits",
                            subtitle = "${daySummary.commitsPercent}% of output"
                        )
                        SplitMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Filled.CallMerge,
                            iconTint = SecondaryEmerald,
                            title = "${daySummary.prCount} Pull Req.",
                            subtitle = "${daySummary.prPercent}% of output"
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SplitMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.RateReview,
                            iconTint = TertiaryAmber,
                            title = "${daySummary.reviewCount} Review",
                            subtitle = "${daySummary.reviewPercent}% of output"
                        )
                        SplitMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.BugReport,
                            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                            title = "${daySummary.issueCount} Issues",
                            subtitle = "${daySummary.issuePercent}% of output"
                        )
                    }
                }
            }
        }

        // 4. Activity Stream Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Stream",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Dec 18, 2024",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Commits 5", "Pull Requests 2").forEach { filter ->
                    val isSelected = filter.startsWith(selectedFilter)
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { selectedFilter = filter.split(" ")[0] },
                        shape = CircleShape,
                        color = if (isSelected) PrimaryEmerald else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) OnPrimaryEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Stream Items
        items(MockData.inspectorActivities) { act ->
            InspectorActivityItemCard(act, maskSensitive)
        }

        // 5. Data Visibility & Privacy Settings Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, OutlineBorder)
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

                    // Switch 1: Include Private Contributions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Include Private Contributions",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Render anonymized activity from enterprise repos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = includePrivate,
                            onCheckedChange = { includePrivate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OnPrimaryEmerald,
                                checkedTrackColor = PrimaryEmerald
                            )
                        )
                    }

                    HorizontalDivider(color = OutlineBorder.copy(alpha = 0.4f), thickness = 1.dp)

                    // Switch 2: Mask Sensitive Repositories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mask Sensitive Repositories",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Obfuscate client project names and file diff stats",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = maskSensitive,
                            onCheckedChange = { maskSensitive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OnPrimaryEmerald,
                                checkedTrackColor = PrimaryEmerald
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplitMetricTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InspectorActivityItemCard(item: GitActivityItem, maskSensitive: Boolean) {
    val displayRepoName = if (maskSensitive) "client-masked / confidential" else item.repoName

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
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
                                else -> PrimaryEmerald
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.commitHash != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = item.commitHash,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (item.statusText != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryDeepContainer
                        ) {
                            Text(
                                text = item.statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryLightAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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

                if (item.additions > 0 || item.deletions > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+${item.additions}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryEmerald
                        )
                        Text(
                            text = "-${item.deletions}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ErrorRed
                        )
                    }
                }
            }
        }
    }
}
