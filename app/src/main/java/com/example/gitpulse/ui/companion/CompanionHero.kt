package com.example.gitpulse.ui.companion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.gitpulse.data.remote.GitPulseAggregateData
import com.example.gitpulse.ui.animation.AnimatedNumberCounter
import com.example.gitpulse.ui.animation.MotionTokens
import java.util.Calendar
import java.util.Locale

/** Working companion name; rename here when the fox earns a real one. */
private const val COMPANION_NAME = "Kitsu"

private enum class CompanionMood(val label: String, val subtitle: String) {
    THRIVING("THRIVING", "$COMPANION_NAME's been thriving this week."),
    STEADY("STEADY", "$COMPANION_NAME's keeping an easy pace."),
    RESTING("RESTING", "$COMPANION_NAME's having a quiet week — no rush.")
}
/**
 * Kitsu companion hero (Phase 0).
 *
 * Replaces the old profile + streak cards: the fox lives on a meadow built
 * from the last 28 contribution days, the greeting and mood chip carry the
 * streak story, and three floating bubbles carry the key numbers. All classic
 * analytics stay below, untouched.
 */
@Composable
fun CompanionHero(
    data: GitPulseAggregateData,
    modifier: Modifier = Modifier
) {
    val profile = data.profile
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.background.luminance() > 0.5f
    val palette = remember(colorScheme, isLight) { kitsuPalette(colorScheme, isLight) }
    val paths = remember { KitsuPaths() }

    val recentDays = remember(data.contributions) {
        data.contributions.sortedBy { it.dateString }.takeLast(28)
    }
    val levels = remember(recentDays) { recentDays.map { it.level } }
    val activeThisWeek = remember(recentDays) { recentDays.takeLast(7).count { it.count > 0 } }
    val mood = when {
        activeThisWeek >= 5 -> CompanionMood.THRIVING
        activeThisWeek >= 2 -> CompanionMood.STEADY
        else -> CompanionMood.RESTING
    }
    val greeting = remember { timeGreeting() }

    val transition = rememberInfiniteTransition(label = "kitsu_idle")
    val breath by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath"
    )
    val tailSway by transition.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tail_sway"
    )
    val ahogeSway by transition.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ahoge_sway"
    )
    val blink by transition.animateFloat(
        1f, 1f,
        infiniteRepeatable(
            keyframes {
                durationMillis = 4600
                1f at 0
                1f at 4150
                0.08f at 4280
                1f at 4420
            },
            RepeatMode.Restart
        ),
        label = "blink"
    )
    val bobA by transition.animateFloat(0f, -4f, infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob_a")
    val bobB by transition.animateFloat(0f, -4f, infiniteRepeatable(tween(3700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob_b")
    val bobC by transition.animateFloat(0f, -4f, infiniteRepeatable(tween(4200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob_c")

    val motion = KitsuMotion(
        blink = blink,
        breath = breath,
        tailSway = tailSway * 2.5f,
        ahogeSway = ahogeSway * 5f
    )
    Column(modifier = modifier.fillMaxWidth()) {
        // Full-bleed scene: no card, the world sits directly on the screen.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawKitsuScene(
                    paths = paths,
                    palette = palette,
                    motion = motion,
                    nightDecor = !isLight,
                    levels = levels
                )
            }

            // Greeting floats over the sky.
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth(0.7f)
                    .padding(start = 16.dp, top = 2.dp)
            ) {
                Text(
                    text = "$greeting, @${profile.username}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = mood.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            MoodChip(
                mood = mood,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 20.dp, top = 2.dp)
            )

            StatBubble(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 16.dp, y = (-26).dp)
                    .offset { IntOffset(0, bobA.dp.roundToPx()) }
            ) {
                AnimatedNumberCounter(
                    count = profile.activeStreakDays,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.primary
                )
                Text(
                    text = "day streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            StatBubble(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-16).dp, y = (-56).dp)
                    .offset { IntOffset(0, bobB.dp.roundToPx()) }
            ) {
                AnimatedNumberCounter(
                    count = profile.commitsToday,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface
                )
                Text(
                    text = "commits today",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            StatBubble(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-16).dp, y = 44.dp)
                    .offset { IntOffset(0, bobC.dp.roundToPx()) }
            ) {
                Text(
                    text = formatCompact(profile.totalAnnualCommits),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface
                )
                Text(
                    text = "this year",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        // Milestone line sits on the screen itself, below the meadow.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Next goal · ${profile.currentGoalDays} days",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${profile.goalPercentage}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary
                )
            }

            val progress by animateFloatAsState(
                targetValue = (profile.goalPercentage / 100f).coerceIn(0f, 1f),
                animationSpec = MotionTokens.SpringBouncy,
                label = "milestone_anim"
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = colorScheme.primary,
                trackColor = colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun MoodChip(mood: CompanionMood, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val accent = when (mood) {
        CompanionMood.THRIVING -> colorScheme.primary
        CompanionMood.STEADY -> colorScheme.secondary
        CompanionMood.RESTING -> colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).background(accent, CircleShape))
            Text(
                text = mood.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun StatBubble(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

private fun timeGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 5 -> "Late night"
        hour < 12 -> "Morning"
        hour < 18 -> "Afternoon"
        else -> "Evening"
    }
}

private fun formatCompact(value: Int): String =
    if (value >= 1000) String.format(Locale.US, "%.1fk", value / 1000f) else "$value"
