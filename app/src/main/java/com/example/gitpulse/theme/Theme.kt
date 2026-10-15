package com.example.gitpulse.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val GitPulseDarkColorScheme = darkColorScheme(
    primary = PrimaryEmerald,
    onPrimary = OnPrimaryEmerald,
    primaryContainer = PrimaryContainerEmerald,
    onPrimaryContainer = OnPrimaryContainerEmerald,
    secondary = SecondaryEmerald,
    onSecondary = OnSecondaryEmerald,
    secondaryContainer = SecondaryContainerEmerald,
    onSecondaryContainer = OnSecondaryContainerEmerald,
    tertiary = TertiaryAmber,
    onTertiary = OnTertiaryAmber,
    tertiaryContainer = TertiaryContainerAmber,
    onTertiaryContainer = OnTertiaryContainerAmber,
    error = ErrorRed,
    onError = OnErrorRed,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed,
    background = SurfaceCanvas,
    onBackground = OnSurface,
    surface = SurfaceCanvas,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    outline = OutlineBorder,
    outlineVariant = OutlineVariant,
    surfaceTint = PrimaryEmerald
)

private val GitPulseAmoledColorScheme = GitPulseDarkColorScheme.copy(
    background = androidx.compose.ui.graphics.Color(0xFF000000),
    surface = androidx.compose.ui.graphics.Color(0xFF000000),
    surfaceContainerLowest = androidx.compose.ui.graphics.Color(0xFF000000),
    surfaceContainerLow = androidx.compose.ui.graphics.Color(0xFF090D14),
    surfaceContainer = androidx.compose.ui.graphics.Color(0xFF10141D),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFF181E29),
    surfaceContainerHighest = androidx.compose.ui.graphics.Color(0xFF222936)
)

private val GitPulseLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnPrimary,
    secondaryContainer = LightSurfaceContainerHigh,
    onSecondaryContainer = LightOnSurface,
    tertiary = LightTertiary,
    onTertiary = LightOnPrimary,
    tertiaryContainer = LightSurfaceContainerHigh,
    onTertiaryContainer = LightTertiary,
    error = ErrorRed,
    onError = OnErrorRed,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed,
    background = LightSurfaceCanvas,
    onBackground = LightOnSurface,
    surface = LightSurfaceCanvas,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceContainerLow,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    outline = LightOutlineBorder,
    outlineVariant = LightOutlineVariant,
    surfaceTint = LightPrimary
)

@Composable
fun GitPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme && amoled -> GitPulseAmoledColorScheme
        darkTheme -> GitPulseDarkColorScheme
        else -> GitPulseLightColorScheme
    }

    // NOTE: material3 1.4.0 (BOM 2026.03.01) keeps MaterialExpressiveTheme /
    // MotionScheme.expressive() internal, so we apply the expressive *look*
    // via theme tokens: expressive shapes + expressive type scale + expressive
    // springs in MotionTokens. Bump to material3 1.5.x + compileSdk 37 to use
    // MaterialExpressiveTheme / LinearWavyProgressIndicator / LoadingIndicator.
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = GitPulseShapes,
        typography = Typography,
        content = content
    )
}
