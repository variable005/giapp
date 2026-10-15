package com.example.gitpulse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class NavigationTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    INSPECTOR("Inspector", Icons.Default.CalendarMonth),
    ANALYTICS("Analytics", Icons.Default.Timeline),
    COMPARE("Compare", Icons.Default.People)
}

/**
 * Material 3 Expressive floating short navigation bar.
 * Uses [ShortNavigationBar] so the indicator, icon morph and expressive motion
 * come from MaterialExpressiveTheme instead of a hand-rolled pill.
 */
@Composable
fun FloatingBottomNav(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            ShortNavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                arrangement = ShortNavigationBarArrangement.EqualWeight
            ) {
                NavigationTab.entries.forEach { tab ->
                    ShortNavigationBarItem(
                        selected = tab == currentTab,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        }
    }
}
