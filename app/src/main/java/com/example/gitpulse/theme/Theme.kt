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
    surface = SurfaceContainer,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = OnSurfaceVariant,
    outline = OutlineBorder,
    outlineVariant = OutlineVariant,
    surfaceTint = PrimaryEmerald
)

private val GitPulseLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnPrimary,
    secondaryContainer = LightPrimaryContainer,
    onSecondaryContainer = LightOnPrimaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnPrimary,
    tertiaryContainer = TertiaryContainerAmber,
    onTertiaryContainer = OnTertiaryContainerAmber,
    error = ErrorRed,
    onError = OnErrorRed,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed,
    background = LightSurfaceCanvas,
    onBackground = LightOnSurface,
    surface = LightSurfaceContainerLowest,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceContainerLow,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutlineBorder,
    outlineVariant = LightOutlineBorder,
    surfaceTint = LightPrimary
)

@Composable
fun GitPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default to GitPulse custom emerald palette for branded feel
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> GitPulseDarkColorScheme
        else -> GitPulseLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
