package com.example.gitpulse.ui.companion

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.gitpulse.data.remote.GitPulseAggregateData
import com.example.gitpulse.ui.animation.MotionTokens
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One-shot poses layered over the idle animation. */
private enum class KitsuReaction { NONE, HAPPY, CURIOUS }

/**
 * Kitsu companion hero (v4, frame flipbook).
 *
 * The fox lives on the app background as a frame-animated flipbook built
 * from the art set (see KitsuFrames.kt, assets built by
 * tools/build_kitsu_frames.py): a soft idle loop she breathes in, with
 * reactions layered over it:
 *  - tap the fox: she pounces at the screen, with a heart burst and a quip
 *  - long-press: she holds a curious head tilt and shows her stat card
 *  - new data: she celebrates the sync by herself with a full spin
 *  - idle: every so often she spins for the fun of it
 *  - reduced-motion settings park her on the calm pose
 *
 * The stat card still grows the tail count with the longest streak
 * (2 at 30 days, 3 at 100), so her progress is tracked as before.
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

    // The flipbook: idle loop, reactions and the frames the canvas draws.
    val framePlayer = rememberKitsuFramePlayer(reducedMotion)

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    // Anchor to today: GitHub's year calendar runs to Dec 31, so a plain
    // takeLast() would read empty future days and the fox would always report
    // a quiet week.
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val recentDays = remember(data.contributions, today) {
        data.contributions
            .filter { it.dateString <= today }
            .sortedBy { it.dateString }
            .takeLast(28)
    }
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
    // Tap hop: one arc up, with a stretch on the way and a squash on landing,
    // so a tap has weight instead of only switching her face.
    val hop = remember { Animatable(0f) }

    fun reactHappy(flourish: KitsuFrameSet) {
        reaction = KitsuReaction.HAPPY
        statsVisible = false
        happyStamp += 1
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (!reducedMotion) {
            framePlayer.playOnce(flourish)
            scope.launch {
                heartBurst.snapTo(0f)
                heartBurst.animateTo(1f, tween(950, easing = FastOutSlowInEasing))
            }
            scope.launch {
                hop.snapTo(0f)
                hop.animateTo(1f, tween(560, easing = FastOutSlowInEasing))
            }
        }
    }

    // Fresh data landed: the fox celebrates the sync with a spin (skips the
    // first load).
    LaunchedEffect(syncTick) {
        if (syncTick != lastSyncTick) {
            lastSyncTick = syncTick
            quip = "Fresh pull! The meadow's up to date."
            reactHappy(KitsuFrameSet.SPIN)
        }
    }

    // Every so often she spins of her own accord, more often when the week
    // is going well. Mood changes re-roll the rhythm.
    LaunchedEffect(mood, reducedMotion) {
        if (reducedMotion) return@LaunchedEffect
        while (true) {
            delay(autoSpinDelayMs(mood))
            framePlayer.playOnce(KitsuFrameSet.SPIN)
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
            framePlayer.resume()
        }
    }

    // Quips dissolve on their own.
    LaunchedEffect(quip) {
        if (quip != null) {
            delay(3000)
            quip = null
        }
    }

    // ----- how alive she feels -----
    // Mood sets tempo and depth; the idle loop supplies the beat; the phone
    // supplies the follow-through. See CompanionMotion / DeviceMotion.
    val moodProfile = remember(mood) { motionProfile(mood) }
    val idle = rememberKitsuIdle(moodProfile, enabled = !reducedMotion)
    val device = rememberDeviceMotion(enabled = !reducedMotion)

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

    val hopArc = sin(PI * hop.value).toFloat()

    val pose = KitsuPose(
        happy = happyBlend,
        tilt = tiltBlend,
        // ~9dp of travel: enough to read as a hop, not so much that she
        // detaches from the ground line she is drawn on.
        hop = hopArc * 9f,
        squash = sin(2.0 * PI * hop.value).toFloat() * 0.35f
    )

    val motion = kitsuMotion(
        idle = idle,
        profile = moodProfile,
        device = device,
        pose = pose,
        reducedMotion = reducedMotion
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
                    tails = tails,
                    frames = framePlayer.paint,
                    // Read here, in the draw pass, so crossfades never
                    // recompose the screen.
                    frameBlend = framePlayer.blend.value
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
                    .padding(top = 40.dp)
                    .size(width = 240.dp, height = 208.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                quip = quips.random()
                                reactHappy(KitsuFrameSet.POUNCE)
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                quip = null
                                statsVisible = true
                                reaction = KitsuReaction.CURIOUS
                                framePlayer.hold(KitsuFrameSet.CURIOUS)
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

            // Tap reaction: a quip bubble under the fox's feet — caption
            // style, so it stays clear of her head and ears.
            KitsuQuipBubble(
                text = quip ?: "",
                visible = quip != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp, start = 24.dp, end = 24.dp)
            )
        }

        // Long-press reaction: the fox's own stat card, unfolded beneath the
        // meadow so it never covers her while she holds the curious tilt.
        KitsuStatsOverlay(
            visible = statsVisible,
            mood = mood,
            tails = tails,
            longestStreak = profile.longestStreakDays,
            totalCommits = profile.totalAnnualCommits,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

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
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp)),
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
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Unfold wrapper for the fox's stat card, shown beneath the scene. */
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
            expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = MotionTokens.springFast()
            ),
        exit = fadeOut(animationSpec = tween(140)) +
            shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(160)
            )
    ) {
        Box(modifier = Modifier.padding(top = 10.dp)) {
            KitsuStatsCard(
                mood = mood,
                tails = tails,
                longestStreak = longestStreak,
                totalCommits = totalCommits
            )
        }
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

/**
 * How long the fox waits before spinning of her own accord. A thriving week
 * has her showing off often; a quiet week has her mostly resting.
 */
internal fun autoSpinDelayMs(mood: CompanionMood): Long = when (mood) {
    CompanionMood.THRIVING -> Random.nextLong(16_000, 26_000)
    CompanionMood.STEADY -> Random.nextLong(22_000, 36_000)
    CompanionMood.RESTING -> Random.nextLong(32_000, 50_000)
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
