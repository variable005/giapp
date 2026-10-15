package com.example.gitpulse.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.gitpulse.theme.SurfaceContainer
import com.example.gitpulse.theme.SurfaceContainerHighest

/**
 * Material 3 Expressive Motion and Easing Tokens
 * Based on https://m3.material.io/styles/motion/easing-and-duration/tokens-specs
 */
object MotionTokens {
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    const val DurationShort = 200
    const val DurationMedium = 350
    const val DurationLong = 500

    // M3 Expressive Spring Specs
    val SpringBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val SpringGentle = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    @Composable
    fun rememberShimmerBrush(): Brush {
        val transition = rememberInfiniteTransition(label = "shimmer_transition")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_anim"
        )

        return Brush.linearGradient(
            colors = listOf(
                SurfaceContainer,
                SurfaceContainerHighest,
                SurfaceContainer
            ),
            start = Offset(translateAnim - 300f, translateAnim - 300f),
            end = Offset(translateAnim, translateAnim)
        )
    }

    @Composable
    fun rememberInfiniteRotation(isActive: Boolean): Float {
        val transition = rememberInfiniteTransition(label = "spin_transition")
        val rotation by transition.animateFloat(
            initialValue = 0f,
            targetValue = if (isActive) 360f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin_anim"
        )
        return if (isActive) rotation else 0f
    }
}

/**
 * Animated rolling number counter with M3 vertical slide & fade transitions.
 */
@Composable
fun AnimatedNumberCounter(
    count: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    suffix: String = ""
) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically(
                    animationSpec = tween(MotionTokens.DurationMedium, easing = MotionTokens.EmphasizedDecelerateEasing)
                ) { it / 2 } + fadeIn(tween(MotionTokens.DurationShort))) togetherWith
                (slideOutVertically(
                    animationSpec = tween(MotionTokens.DurationMedium, easing = MotionTokens.EmphasizedAccelerateEasing)
                ) { -it / 2 } + fadeOut(tween(MotionTokens.DurationShort)))
            } else {
                (slideInVertically(
                    animationSpec = tween(MotionTokens.DurationMedium, easing = MotionTokens.EmphasizedDecelerateEasing)
                ) { -it / 2 } + fadeIn(tween(MotionTokens.DurationShort))) togetherWith
                (slideOutVertically(
                    animationSpec = tween(MotionTokens.DurationMedium, easing = MotionTokens.EmphasizedAccelerateEasing)
                ) { it / 2 } + fadeOut(tween(MotionTokens.DurationShort)))
            }
        },
        label = "animated_counter",
        modifier = modifier
    ) { targetCount ->
        Text(
            text = "$targetCount$suffix",
            style = style,
            color = color
        )
    }
}

/**
 * Pulsing status dot indicating active live synchronization with infinite breathing ring.
 */
@Composable
fun PulsingStatusDot(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_halo")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_alpha"
    )

    Box(
        modifier = modifier.size(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing wave
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(pulseScale)
                .alpha(pulseAlpha)
                .background(color, CircleShape)
        )
        // Solid center dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
    }
}

