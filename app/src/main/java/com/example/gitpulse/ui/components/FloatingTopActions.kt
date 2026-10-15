package com.example.gitpulse.ui.components

import androidx.compose.animation.core.animateFloatAsState
import com.example.gitpulse.ui.animation.MotionTokens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.example.gitpulse.theme.*

/**
 * Material 3 Expressive floating top action bar.
 * Larger touch targets (52dp), 20dp+ icons and labelMedium emphasis
 * so pills read as expressive rather than compact M3.
 */
@Composable
fun FloatingTopActions(
    currentUsername: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isLoading: Boolean = false,
    hasUnreadNotifications: Boolean = true,
    modifier: Modifier = Modifier
) {
    val themeRotation by animateFloatAsState(
        targetValue = if (isDarkTheme) 0f else 180f,
        animationSpec = MotionTokens.SpringBouncy,
        label = "theme_rot"
    )

    val refreshRotation = MotionTokens.rememberInfiniteRotation(isActive = isLoading)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Dark/Light Mode Switcher Pill
        Surface(
            modifier = Modifier
                .height(52.dp)
                .clip(CircleShape)
                .clickable { onToggleTheme() },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 3.dp,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(themeRotation)
                )
                Text(
                    text = if (isDarkTheme) "Dark" else "Light",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Center: Interactive Active User Search Pill
        Surface(
            modifier = Modifier
                .height(52.dp)
                .clip(CircleShape)
                .clickable { onSearchClick() },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            tonalElevation = 4.dp,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search User",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "@$currentUsername",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Right Action Group: Refresh + Notifications + Settings
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live API Refresh Button with animated spinner
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .clickable { onRefreshClick() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 3.dp,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Data",
                        tint = if (isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(refreshRotation)
                    )
                }
            }

            // Notification Bell with Unread Badge
            Box {
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { onNotificationsClick() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    tonalElevation = 3.dp,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .size(12.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    )
                }
            }

            // Settings Button
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .clickable { onSettingsClick() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 3.dp,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
