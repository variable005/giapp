package com.example.gitpulse.ui.companion

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.gitpulse.R
import com.example.gitpulse.data.remote.GitPulseAggregateData
import com.example.gitpulse.ui.animation.MotionTokens
import java.util.Calendar
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One-shot poses layered over the idle animation. */
private enum class KitsuReaction { NONE, HAPPY, CURIOUS }

/**
 * Kitsu companion hero (v3).
 *
 * The fox lives on a meadow built from the last 28 contribution days and is
 * now properly interactive:
 *  - tap the fox: heart burst and a quip in a speech bubble
 *  - long-press: the fox tilts its head and shows its stat card
 *  - new data: the fox celebrates the sync by itself
 *  - idle: breathing, tail sway, blinking and the occasional ear twitch
 *  - reduced-motion settings keep everything calm and static
 *
 * Tails grow with the longest streak (1 -> 2 at 30 days, 3 at 100 days), so
 * the fox visibly evolves with the user and never regresses.
 */
@Composable
fun CompanionHero(
    data: GitPulseAggregateData,
    syncTick: Int = 0,
    modifier: Modifier = Modifier
) {
    val profile = data.profile
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.background.luminance() > 0.5f
    val palette = remember(colorScheme, isLight) { kitsuPalette(colorScheme, isLight) }
    val paths = remember { KitsuPaths() }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val reducedMotion = rememberReducedMotion()

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val recentDays = remember(data.contributions) {
        data.contributions.sortedBy { it.dateString }.takeLast(28)
    }
    val levels = remember(recentDays) { recentDays.map { it.level } }
    val activeThisWeek = remember(recentDays) { recentDays.takeLast(7).count { it.count > 0 } }
    val mood = companionMood(activeThisWeek)
    val tails = kitsuTailCount(profile.longestStreakDays)
    val greeting = remember(hour) { timeGreeting(hour) }
    val quips = remember(mood, hour) { kitsuQuips(mood, hour) }

    // Interaction state.
    var quip by remember { mutableStateOf<String?>(null) }
    var statsVisible by remember { mutableStateOf(false) }
    var reaction by remember { mutableStateOf(KitsuReaction.NONE) }
    var happyStamp by remember { mutableStateOf(0) }
    var lastSyncTick by remember { mutableStateOf(syncTick) }

    val heartBurst = remember { Animatable(1f) }

    fun reactHappy() {
        reaction = KitsuReaction.HAPPY
        statsVisible = false
        happyStamp += 1
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (!reducedMotion) {
            scope.launch {
                heartBurst.snapTo(0f)
                heartBurst.animateTo(1f, tween(950, easing = FastOutSlowInEasing))
            }
        }
    }

    // Fresh data landed: the fox celebrates the sync (skips the first load).
    LaunchedEffect(syncTick) {
        if (syncTick != lastSyncTick) {
            lastSyncTick = syncTick
            quip = "Fresh pull! The meadow's up to date."
            reactHappy()
        }
    }

    // Happy pose fades back after every tap (happyStamp restarts the timer).
    LaunchedEffect(happyStamp) {
        if (happyStamp > 0) {
            delay(2100)
            if (reaction == KitsuReaction.HAPPY) reaction = KitsuReaction.NONE
        }
    }

    // The stats card times out; the curious tilt leaves with it.
    LaunchedEffect(statsVisible) {
        if (statsVisible) {
            delay(5200)
            statsVisible = false
        } else if (reaction == KitsuReaction.CURIOUS) {
            reaction = KitsuReaction.NONE
        }
    }

    // Quips dissolve on their own.
    LaunchedEffect(quip) {
        if (quip != null) {
            delay(3000)
            quip = null
        }
    }

    // ----- idle life -----
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
    val sway by transition.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(4400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway"
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
    val earTwitch by transition.animateFloat(
        0f, 0f,
        infiniteRepeatable(
            keyframes {
                durationMillis = 5400
                0f at 0
                0f at 4300
                -7f at 4460
                0f at 4620
            },
            RepeatMode.Restart
        ),
        label = "ear_twitch"
    )
    // Slow ambient loop (drifting leaves by day, fireflies at night).
    val ambientPhase by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(60000, easing = LinearEasing), RepeatMode.Restart),
        label = "ambient"
    )

    val happyBlend by animateFloatAsState(
        targetValue = if (reaction == KitsuReaction.HAPPY) 1f else 0f,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "kitsu_happy"
    )
    val tiltBlend by animateFloatAsState(
        targetValue = if (reaction == KitsuReaction.CURIOUS) -6f else 0f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "kitsu_tilt"
    )

    val motion = if (reducedMotion) {
        KitsuMotion(blink = 1f, breath = 0f, tailSway = 0f, ahogeSway = 0f)
    } else {
        KitsuMotion(
            blink = blink,
            breath = breath,
            tailSway = tailSway * (2.5f + happyBlend * 1.8f),
            ahogeSway = ahogeSway * 5f,
            earTwitch = earTwitch,
            happy = happyBlend,
            tilt = tiltBlend,
            sway = sway
        )
    }

    // Companion art shipped with the app: drawn on the meadow crest when
    // present; the hand-drawn vector fox stays as the fallback.
    val companionArt = ImageBitmap.imageResource(R.drawable.kitsu_companion)

    // Illustrated meadow the companion stands on; the drawn ground stays
    // as a fallback whenever the art is absent.
    val meadowArt = ImageBitmap.imageResource(R.drawable.kitsu_meadow)

    // Fluid rig: warps the art through a GPU mesh each frame. If setup ever
    // fails the scene quietly falls back to the static drawImage path.
    val companionWarp = remember(companionArt) {
        runCatching { CompanionWarp(companionArt) }.getOrNull()
    }

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
                    sunUp = isLight && hour in 6..17,
                    tails = tails,
                    levels = levels,
                    companion = companionArt,
                    meadow = meadowArt,
                    companionWarp = companionWarp,
                    reducedMotion = reducedMotion,
                    ambientPhase = ambientPhase
                )

                // Heart burst on tap: a handful of hearts drifting upward.
                val burst = heartBurst.value
                if (burst < 1f) {
                    val centerX = size.width / 2f
                    val spawnY = size.height * 0.70f - 96.dp.toPx()
                    val spreads = listOf(-46f, -28f, -10f, 10f, 28f, 46f)
                    spreads.forEachIndexed { i, spreadDp ->
                        val lift = ((36f + 26f * (i % 3)) * burst).dp.toPx()
                        val x = centerX + spreadDp.dp.toPx() * (0.75f + 0.25f * (i % 3) / 2f) +
                            sin(burst * 6f + i * 1.3f) * 5.dp.toPx()
                        val y = spawnY - lift
                        val alpha = ((1f - burst) * 0.9f).coerceIn(0f, 1f)
                        val color = if (i % 3 == 0) {
                            palette.emerald.copy(alpha = alpha)
                        } else {
                            palette.blush.copy(alpha = alpha)
                        }
                        drawCompanionHeart(
                            center = Offset(x, y),
                            radius = 5.5f.dp.toPx() * (0.7f + 0.3f * (i % 2)),
                            color = color
                        )
                    }
                }
            }

            // (Kitsu3DFox.kt — the SceneView 3D pipeline — stays in the tree,
            // unmounted, ready for a future 3D return.)

            // Invisible tap target wrapped around the companion.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 52.dp)
                    .size(width = 194.dp, height = 168.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                quip = quips.random()
                                reactHappy()
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                quip = null
                                statsVisible = true
                                reaction = KitsuReaction.CURIOUS
                            }
                        )
                    }
            )

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

            // Tap reaction: a quip bubble above the fox's head.
            KitsuQuipBubble(
                text = quip ?: "",
                visible = quip != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp, start = 24.dp, end = 24.dp)
            )

            // Long-press reaction: the fox's own stat card.
            KitsuStatsOverlay(
                visible = statsVisible,
                mood = mood,
                tails = tails,
                longestStreak = profile.longestStreakDays,
                totalCommits = profile.totalAnnualCommits,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 46.dp)
            )
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
    val accent = moodAccent(mood, colorScheme)

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

/** Quip bubble shown after a tap; keeps its last text while fading out. */
@Composable
private fun KitsuQuipBubble(text: String, visible: Boolean, modifier: Modifier = Modifier) {
    var lastText by remember { mutableStateOf(text) }
    if (text.isNotEmpty()) lastText = text

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(160)) +
            scaleIn(initialScale = 0.85f, animationSpec = MotionTokens.springBouncy()),
        exit = fadeOut(animationSpec = tween(140)) +
            scaleOut(targetScale = 0.92f, animationSpec = tween(140))
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.97f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text(
                text = lastText,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Slide-in wrapper for the fox's stat card. */
@Composable
private fun KitsuStatsOverlay(
    visible: Boolean,
    mood: CompanionMood,
    tails: Int,
    longestStreak: Int,
    totalCommits: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(180)) +
            scaleIn(initialScale = 0.9f, animationSpec = MotionTokens.springFast()),
        exit = fadeOut(animationSpec = tween(140)) +
            scaleOut(targetScale = 0.92f, animationSpec = tween(140))
    ) {
        KitsuStatsCard(
            mood = mood,
            tails = tails,
            longestStreak = longestStreak,
            totalCommits = totalCommits
        )
    }
}

/** The fox's own little stat sheet, shown on long-press. */
@Composable
private fun KitsuStatsCard(
    mood: CompanionMood,
    tails: Int,
    longestStreak: Int,
    totalCommits: Int
) {
    val colorScheme = MaterialTheme.colorScheme
    val nextTail = nextTailMilestone(longestStreak)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surfaceContainerHigh.copy(alpha = 0.97f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .width(240.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$COMPANION_NAME · companion",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = mood.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = moodAccent(mood, colorScheme)
                )
            }
            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.6f))
            StatLine(label = "Tails earned", value = "$tails of 3")
            Text(
                text = if (nextTail != null) {
                    "Next tail grows at a $nextTail-day streak."
                } else {
                    "Fully evolved — all tails grown."
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (nextTail == null) colorScheme.primary else colorScheme.onSurfaceVariant
            )
            StatLine(label = "Longest streak", value = "$longestStreak days")
            StatLine(label = "Commits this year", value = "$totalCommits")
            Text(
                text = "Tap me anytime — I like the company.",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun moodAccent(mood: CompanionMood, colorScheme: ColorScheme): Color = when (mood) {
    CompanionMood.THRIVING -> colorScheme.primary
    CompanionMood.STEADY -> colorScheme.secondary
    CompanionMood.RESTING -> colorScheme.onSurfaceVariant
}

/** Honors the system's "remove animations" accessibility setting: with the
 *  animator scale at zero the fox holds a calm, static pose. */
@Composable
private fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
