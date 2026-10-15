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

    // M3 Expressive spring specs (match ExpressiveMotionTokens).
    // Default spatial: damping 0.8, stiffness 380. Fast spatial: 0.6 / 800.
    val SpringBouncy = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = 380f
    )

    fun <T> springBouncy(): SpringSpec<T> = spring(
        dampingRatio = 0.8f,
        stiffness = 380f
    )

    fun <T> springFast(): SpringSpec<T> = spring(
        dampingRatio = 0.6f,
        stiffness = 800f
    )

    fun <T> springGentle(): SpringSpec<T> = spring(
        dampingRatio = 0.9f,
        stiffness = 600f
    )

    val SpringFastSpatial = spring<Float>(
        dampingRatio = 0.6f,
        stiffness = 800f
    )

    val SpringSlowSpatial = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = 200f
    )

    val SpringGentle = spring<Float>(
        dampingRatio = 1f,
        stiffness = 1600f
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
 * Animated rolling number counter with M3 Expressive physics spring transitions.
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
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) { it / 2 } + fadeIn(animationSpec = tween(180, easing = LinearOutSlowInEasing))) togetherWith
                (slideOutVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) { -it / 2 } + fadeOut(animationSpec = tween(150, easing = FastOutLinearInEasing)))
            } else {
                (slideInVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) { -it / 2 } + fadeIn(animationSpec = tween(180, easing = LinearOutSlowInEasing))) togetherWith
                (slideOutVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) { it / 2 } + fadeOut(animationSpec = tween(150, easing = FastOutLinearInEasing)))
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
 * Static clean status indicator dot. Pulsating animation removed as requested.
 */
@Composable
fun PulsingStatusDot(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(7.dp)
            .background(color, CircleShape)
    )
}

