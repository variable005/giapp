package com.example.gitpulse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gitpulse.data.ActivityType
import com.example.gitpulse.data.GitActivityItem
import com.example.gitpulse.theme.SecondaryEmerald
import com.example.gitpulse.theme.TertiaryAmber

/**
 * Activity stream shared by the dashboard ("Recent Activity Stream") and the
 * inspector ("Live Activity Stream").
 *
 * A stream is a list, not a stack of cards: every entry is a dense row inside
 * one container, separated by hairline dividers, and the type is carried by a
 * single colored badge so the eye can scan a whole column in one pass.
 *
 * When [collapsedCount] is smaller than the item count the tail is hidden
 * behind a "Show N more" toggle, which keeps the screen height predictable.
 */
@Composable
fun ActivityStreamList(
    items: List<GitActivityItem>,
    modifier: Modifier = Modifier,
    maskSensitive: Boolean = false,
    collapsedCount: Int = Int.MAX_VALUE
) {
    if (items.isEmpty()) return

    var expanded by remember(items) { mutableStateOf(false) }
    val visible = if (expanded) items else items.take(collapsedCount)
    val hiddenCount = items.size - visible.size

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                visible.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                    ActivityStreamRow(item = item, maskSensitive = maskSensitive)
                }
            }
        }

        if (hiddenCount > 0 || expanded) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (expanded) "Show less" else "Show $hiddenCount more",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun ActivityStreamRow(
    item: GitActivityItem,
    maskSensitive: Boolean
) {
    val repoName = if (maskSensitive) "confidential / masked-repo" else item.repoName
    val accent = when (item.type) {
        ActivityType.PULL_REQUEST -> SecondaryEmerald
        ActivityType.REVIEW -> TertiaryAmber
        else -> MaterialTheme.colorScheme.primary
    }
    val typeIcon = when (item.type) {
        ActivityType.PULL_REQUEST -> Icons.AutoMirrored.Filled.CallMerge
        ActivityType.REVIEW -> Icons.Default.RateReview
        else -> Icons.Default.Commit
    }
    val hasMeta = item.commitHash != null || item.prNumber != null ||
        item.branch != null || item.statusText != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = typeIcon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = repoName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = item.timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Text(
                text = item.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (hasMeta) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.commitHash?.let { hash ->
                            MetaChip(text = hash, color = MaterialTheme.colorScheme.primary)
                        }
                        item.prNumber?.let { pr ->
                            MetaChip(text = pr, color = SecondaryEmerald)
                        }
                        item.branch?.let { branch ->
                            Text(
                                text = branch,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    item.statusText?.let { status ->
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusPill(status = status)
                    }
                }
            }
        }
    }
}

/** Compact token for a commit hash or PR number. */
@Composable
private fun MetaChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

/** Neutral state pill: reads for conclusions ("Merged") and pending states
 *  ("3 reviewers assigned") alike. */
@Composable
private fun StatusPill(status: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}
