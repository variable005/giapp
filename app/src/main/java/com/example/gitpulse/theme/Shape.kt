package com.example.gitpulse.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material 3 Expressive shape scale.
// Baseline M3: extraSmall 4dp, small 8dp, medium 12dp, large 16dp, extraLarge 28dp.
// Expressive adds increased + XXL variants for hero containers and morphing buttons.
val GitPulseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Common expressive container shapes used across cards / sheets / dialogs.
val ExpressiveCardShape = RoundedCornerShape(28.dp)
val ExpressiveLargeCardShape = RoundedCornerShape(32.dp)
val ExpressiveMediumCardShape = RoundedCornerShape(20.dp)
val ExpressiveSmallCardShape = RoundedCornerShape(16.dp)
val ExpressiveSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
