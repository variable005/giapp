package com.example.gitpulse.ui.companion

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.example.gitpulse.theme.PrimaryEmerald
import com.example.gitpulse.theme.TertiaryAmber

/**
 * Kitsu companion art (Phase 0).
 *
 * All fox geometry lives in a fixed 200 x 185 virtual space so the Paths can be
 * built once and reused under a single scene transform. Stroke widths are
 * expressed in that same virtual space and scale with the fox.
 *
 * The scene (sky decor, rolling field, contribution meadow) is drawn in
 * canvas pixel space so it stretches with the screen while the fox keeps its
 * proportions. The scene paints no sky: it sits directly on the screen
 * background, full bleed, as if it were part of the app itself.
 */

/** Paths for one idle chibi fox, in the 200 x 185 virtual space. */
class KitsuPaths {
    val shadow = Path().apply { addOval(Rect(54f, 168.5f, 146f, 181.5f)) }

    val tail = Path().apply {
        moveTo(82f, 150f)
        cubicTo(46f, 158f, 12f, 128f, 16f, 90f)
        cubicTo(19f, 64f, 30f, 52f, 42f, 50f)
    }
    val tailTip = Path().apply {
        moveTo(33f, 62f)
        cubicTo(35f, 56f, 38f, 52f, 42f, 50f)
    }
    val tailShade = Path().apply {
        moveTo(74f, 146f)
        cubicTo(46f, 150f, 24f, 128f, 26f, 100f)
    }

    val ear = Path().apply {
        moveTo(58f, 72f)
        cubicTo(57.5f, 44f, 64f, 22f, 72f, 16f)
        cubicTo(84f, 25f, 91.5f, 42f, 94f, 58f)
        cubicTo(84f, 66f, 70f, 71f, 58f, 72f)
        close()
    }
    val earInner = Path().apply {
        moveTo(69f, 63f)
        cubicTo(69f, 45f, 72f, 32f, 75f, 25f)
        cubicTo(81.5f, 33f, 85f, 45f, 86f, 54f)
        cubicTo(80.5f, 59.5f, 73.5f, 62.5f, 69f, 63f)
        close()
    }

    val ahoge = Path().apply {
        moveTo(102f, 42f)
        cubicTo(99f, 33f, 101f, 27f, 107f, 23f)
    }

    val body = Path().apply { addOval(Rect(63f, 111f, 137f, 173f)) }
    val chest = Path().apply { addOval(Rect(79f, 136f, 121f, 166f)) }
    val head = Path().apply { addOval(Rect(54f, 40f, 146f, 132f)) }

    val scarf = Path().apply {
        moveTo(67f, 119f)
        cubicTo(84f, 132f, 116f, 132f, 133f, 119f)
        cubicTo(133f, 128f, 132f, 133f, 130f, 137f)
        cubicTo(112f, 149f, 88f, 149f, 70f, 137f)
        cubicTo(68f, 133f, 67f, 128f, 67f, 119f)
        close()
    }

    val muzzle = Path().apply { addOval(Rect(81f, 92.5f, 119f, 115.5f)) }
    val nose = Path().apply {
        moveTo(95.5f, 96f)
        cubicTo(97f, 93.5f, 103f, 93.5f, 104.5f, 96f)
        cubicTo(103.5f, 100f, 101.5f, 101.5f, 100f, 101.5f)
        cubicTo(98.5f, 101.5f, 96.5f, 100f, 95.5f, 96f)
        close()
    }
    val mouth = Path().apply {
        moveTo(94f, 104f)
        quadraticBezierTo(100f, 108.5f, 106f, 104f)
    }
    val neckShadow = Path().apply {
        moveTo(64f, 112f)
        cubicTo(80f, 123f, 120f, 123f, 136f, 112f)
        cubicTo(131f, 119f, 118f, 125f, 100f, 125f)
        cubicTo(82f, 125f, 69f, 119f, 64f, 112f)
        close()
    }

    val eye = Path().apply {
        moveTo(68.5f, 88f)
        cubicTo(70f, 78f, 78f, 73f, 85f, 78f)
        cubicTo(88f, 81f, 87.5f, 86f, 84.5f, 89f)
        cubicTo(78f, 93.5f, 71f, 92.5f, 68.5f, 88f)
        close()
    }
    val eyeLid = Path().apply {
        moveTo(68.5f, 88f)
        cubicTo(70f, 78f, 78f, 73f, 85f, 78f)
    }
    val cheek = Path().apply { addOval(Rect(57.5f, 98.5f, 70.5f, 107.5f)) }
    val paw = Path().apply { addOval(Rect(71.5f, 157f, 94.5f, 175f)) }
}
/** Live motion values driven by the idle animation loop. */
data class KitsuMotion(
    val blink: Float,
    val breath: Float,
    val tailSway: Float,
    val ahogeSway: Float
)

/** Palette for the scene and the fox. Character tones are brand-locked; the
 *  scenery follows the active Material color scheme. */
class KitsuPalette(
    val line: Color,
    val fur: Color,
    val furLight: Color,
    val amber: Color,
    val amberDeep: Color,
    val eye: Color,
    val nose: Color,
    val blush: Color,
    val emerald: Color,
    val emeraldDeep: Color,
    val hillFar: Color,
    val hillNear: Color,
    val groundLine: Color,
    val grass: Color,
    val dry: Color,
    val flower: Color,
    val star: Color,
    val starDim: Color,
    val sparkle: Color,
    val cloud: Color
)

fun kitsuPalette(colorScheme: ColorScheme, isLight: Boolean): KitsuPalette = KitsuPalette(
    line = if (isLight) Color(0xFF6B5A47) else Color(0xFF37322B),
    fur = Color(0xFFF7E8D0),
    furLight = Color(0xFFFFF7EA),
    amber = TertiaryAmber,
    amberDeep = Color(0xFFF0A254),
    eye = Color(0xFF2E2A38),
    nose = Color(0xFF4A3830),
    blush = Color(0xFFEE9A6B),
    emerald = PrimaryEmerald,
    emeraldDeep = Color(0xFF2FA85F),
    hillFar = colorScheme.surfaceContainerHigh.copy(alpha = if (isLight) 0.9f else 0.5f),
    hillNear = colorScheme.surfaceContainerHighest.copy(alpha = if (isLight) 0.9f else 0.7f),
    groundLine = Color.Black.copy(alpha = if (isLight) 0.08f else 0.25f),
    grass = if (isLight) Color(0xFF2FA85F) else PrimaryEmerald,
    dry = if (isLight) Color(0xFF9AA09A) else Color(0xFF6B6F66),
    flower = if (isLight) Color(0xFFE29100) else TertiaryAmber,
    star = Color(0xFF4A5160),
    starDim = Color(0xFF414855),
    sparkle = Color(0xFF565D6B),
    cloud = Color(0xFFE7E8EC)
)
private val GrassAlphas = floatArrayOf(0f, 0.5f, 0.68f, 0.85f, 1f)

/** Soft second fur tone used for the neck shadow and inner tail shading. */
private val FurShade = Color(0xFFEAD3AE)

/** Faint background tufts scattered under the crest so the field below the
 *  meadow reads as a continuation of the grass rather than empty ground. */
private val ScatterTufts = listOf(
    0.06f to 0.86f, 0.14f to 0.92f, 0.23f to 0.88f, 0.33f to 0.95f, 0.44f to 0.90f,
    0.56f to 0.87f, 0.67f to 0.94f, 0.78f to 0.89f, 0.90f to 0.93f
)

/** Y of the field crest at horizontal position [x]: a single gentle quadratic
 *  from (0, 0.76h) to (w, 0.76h), cresting at 0.72h in the middle. */
private fun groundY(x: Float, w: Float, h: Float): Float {
    val t = (x / w).coerceIn(0f, 1f)
    return (1 - t) * (1 - t) * (0.76f * h) + 2 * (1 - t) * t * (0.72f * h) + t * t * (0.76f * h)
}

/**
 * Draws the full scene: sky decor, rolling hills, the contribution meadow
 * and the fox standing on the field crest. No sky is painted; the scene
 * blends straight into the screen background.
 */
fun DrawScope.drawKitsuScene(
    paths: KitsuPaths,
    palette: KitsuPalette,
    motion: KitsuMotion,
    nightDecor: Boolean,
    levels: List<Int>
) {
    val w = size.width
    val h = size.height

    // Sky decor: stars at night, flat clouds by day.
    if (nightDecor) {
        drawCircle(palette.star, 1.7.dp.toPx(), Offset(0.111f * w, 0.15f * h))
        drawCircle(palette.starDim, 1.3.dp.toPx(), Offset(0.215f * w, 0.09f * h))
        drawCircle(palette.star, 1.5.dp.toPx(), Offset(0.354f * w, 0.17f * h))
        drawCircle(palette.starDim, 1.2.dp.toPx(), Offset(0.472f * w, 0.08f * h))
        drawCircle(palette.star, 1.6.dp.toPx(), Offset(0.91f * w, 0.43f * h))
        drawPath(sparklePath(0.583f * w, 0.221f * h, 6.dp.toPx()), palette.sparkle)
    } else {
        drawCircle(palette.cloud, 9.dp.toPx(), Offset(0.181f * w, 0.23f * h))
        drawCircle(palette.cloud, 11.dp.toPx(), Offset(0.222f * w, 0.21f * h))
        drawCircle(palette.cloud, 8.dp.toPx(), Offset(0.264f * w, 0.235f * h))
        drawCircle(palette.cloud, 7.dp.toPx(), Offset(0.826f * w, 0.37f * h))
        drawCircle(palette.cloud, 8.5.dp.toPx(), Offset(0.861f * w, 0.355f * h))
        drawCircle(palette.cloud, 6.dp.toPx(), Offset(0.896f * w, 0.375f * h))
    }

    // Rolling field: two hill silhouettes run down to the scene's bottom edge.
    val farHill = Path().apply {
        moveTo(0f, 0.74f * h)
        quadraticBezierTo(0.25f * w, 0.52f * h, 0.52f * w, 0.70f * h)
        quadraticBezierTo(0.78f * w, 0.86f * h, w, 0.67f * h)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(farHill, palette.hillFar)

    val nearHill = Path().apply {
        moveTo(0f, 0.80f * h)
        quadraticBezierTo(0.31f * w, 0.63f * h, 0.62f * w, 0.76f * h)
        quadraticBezierTo(0.93f * w, 0.88f * h, w, 0.75f * h)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(nearHill, palette.hillNear)

    // The crest line the meadow roots on: one gentle arc, no band edges.
    val groundLine = Path().apply {
        moveTo(0f, 0.76f * h)
        quadraticBezierTo(0.5f * w, 0.72f * h, w, 0.76f * h)
    }
    drawPath(groundLine, palette.groundLine, style = Stroke(width = 2.dp.toPx()))

    drawMeadow(palette, levels, w, h)
    drawScatteredTufts(palette, w, h)

    // Soft contact shadow where the fox meets the crest.
    val groundCenter = groundY(w / 2f, w, h)
    drawOval(
        color = Color.Black.copy(alpha = 0.15f),
        topLeft = Offset(w / 2f - 52.dp.toPx(), groundCenter),
        size = Size(104.dp.toPx(), 14.dp.toPx())
    )

    // Fox: fixed 145dp tall, centered, standing on the crest.
    val foxScale = 145.dp.toPx() / 185f
    val foxTx = w / 2f - 100f * foxScale
    val foxTy = groundCenter + 5.dp.toPx() - 185f * foxScale
    withTransform({
        translate(foxTx, foxTy)
        scale(foxScale, foxScale, pivot = Offset.Zero)
    }) {
        drawKitsu(paths, palette, motion)
    }
}
/** One dry dash per zero day, grass tufts (3 blades) otherwise; level 4 days
 *  grow a tiny flower. The strip mirrors the annual heatmap's activity tiers
 *  and roots on the field crest curve. */
private fun DrawScope.drawMeadow(palette: KitsuPalette, levels: List<Int>, w: Float, h: Float) {
    if (levels.isEmpty()) return
    val cellW = w / levels.size
    val bladeW = 2.dp.toPx()

    levels.forEachIndexed { index, level ->
        val x = cellW * (index + 0.5f)
        val base = groundY(x, w, h)
        if (level <= 0) {
            drawLine(
                palette.dry.copy(alpha = 0.6f),
                Offset(x - 3.dp.toPx(), base),
                Offset(x + 3.dp.toPx(), base),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
            return@forEachIndexed
        }

        val alpha = GrassAlphas[level.coerceIn(0, 4)]
        val bladeH = (7f + level * 3.6f).dp.toPx()
        val bladeColor = palette.grass.copy(alpha = alpha)

        drawPath(
            bladePath(x, base, x - 1.dp.toPx(), base - bladeH * 0.6f, x - 2.5.dp.toPx(), base - bladeH),
            bladeColor, style = Stroke(bladeW, cap = StrokeCap.Round)
        )
        drawPath(
            bladePath(x - 2.dp.toPx(), base, x - 4.dp.toPx(), base - bladeH * 0.45f, x - 6.dp.toPx(), base - bladeH * 0.72f),
            bladeColor.copy(alpha = alpha * 0.9f), style = Stroke(bladeW, cap = StrokeCap.Round)
        )
        drawPath(
            bladePath(x + 2.dp.toPx(), base, x + 4.dp.toPx(), base - bladeH * 0.5f, x + 5.5.dp.toPx(), base - bladeH * 0.8f),
            bladeColor.copy(alpha = alpha * 0.9f), style = Stroke(bladeW, cap = StrokeCap.Round)
        )

        if (level >= 4) {
            drawCircle(
                palette.flower,
                radius = 1.8.dp.toPx(),
                center = Offset(x - 2.5.dp.toPx(), base - bladeH - 1.5.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawScatteredTufts(palette: KitsuPalette, w: Float, h: Float) {
    ScatterTufts.forEachIndexed { i, (fx, fy) ->
        val x = fx * w
        val base = fy * h
        val bladeH = (5f + (i % 3) * 1.5f).dp.toPx()
        val color = palette.grass.copy(alpha = 0.28f)
        drawPath(
            bladePath(x, base, x - 0.8.dp.toPx(), base - bladeH * 0.6f, x - 2.dp.toPx(), base - bladeH),
            color, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            bladePath(x + 1.5f.dp.toPx(), base, x + 3.dp.toPx(), base - bladeH * 0.5f, x + 4.dp.toPx(), base - bladeH * 0.75f),
            color.copy(alpha = 0.2f), style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun bladePath(x0: Float, y0: Float, cx: Float, cy: Float, x1: Float, y1: Float) =
    Path().apply {
        moveTo(x0, y0)
        quadraticBezierTo(cx, cy, x1, y1)
    }

private fun sparklePath(cx: Float, cy: Float, d: Float) = Path().apply {
    val i = d * 0.3f
    moveTo(cx, cy - d)
    lineTo(cx + i, cy - i)
    lineTo(cx + d, cy)
    lineTo(cx + i, cy + i)
    lineTo(cx, cy + d)
    lineTo(cx - i, cy + i)
    lineTo(cx - d, cy)
    lineTo(cx - i, cy - i)
    close()
}
// ---------------------------------------------------------------- fox body

private fun DrawScope.drawKitsu(paths: KitsuPaths, palette: KitsuPalette, motion: KitsuMotion) {
    drawPath(paths.shadow, Color.Black.copy(alpha = 0.15f))

    // Tail sways around its base.
    withTransform({ rotate(degrees = motion.tailSway, pivot = Offset(80f, 150f)) }) {
        drawPath(paths.tail, palette.line, style = Stroke(39f, cap = StrokeCap.Round))
        drawPath(paths.tail, palette.fur, style = Stroke(32f, cap = StrokeCap.Round))
        drawPath(paths.tailShade, FurShade.copy(alpha = 0.7f), style = Stroke(9f, cap = StrokeCap.Round))
        drawPath(paths.tailTip, palette.line, style = Stroke(39f, cap = StrokeCap.Round))
        drawPath(paths.tailTip, palette.amber, style = Stroke(32f, cap = StrokeCap.Round))
    }

    // Breathing subtly stretches the whole sprite from the ground up.
    withTransform({ scale(1f, 1f + motion.breath * 0.012f, pivot = Offset(100f, 185f)) }) {
        inked(paths.body, palette.fur, palette.line)
        drawPath(paths.chest, palette.furLight)

        ear(paths, palette)
        mirrored { ear(paths, palette) }

        withTransform({ rotate(degrees = motion.ahogeSway, pivot = Offset(102f, 42f)) }) {
            drawPath(paths.ahoge, palette.line, style = Stroke(9f, cap = StrokeCap.Round))
            drawPath(paths.ahoge, palette.fur, style = Stroke(5.5f, cap = StrokeCap.Round))
        }

        inked(paths.head, palette.fur, palette.line)
        drawPath(paths.neckShadow, FurShade.copy(alpha = 0.8f))

        inked(paths.scarf, palette.emerald, palette.line)
        drawCircle(palette.emeraldDeep, radius = 5.5f, center = Offset(100f, 139f))
        drawCircle(palette.line, radius = 5.5f, center = Offset(100f, 139f), style = Stroke(2f))

        drawPath(paths.muzzle, palette.furLight)
        drawPath(paths.nose, palette.nose)
        drawPath(paths.mouth, palette.nose, style = Stroke(2.2f, cap = StrokeCap.Round))

        // Blink squashes both eyes vertically.
        withTransform({ scale(1f, motion.blink, pivot = Offset(78f, 87f)) }) {
            drawEye(paths, palette)
        }
        mirrored {
            withTransform({ scale(1f, motion.blink, pivot = Offset(78f, 87f)) }) {
                drawEye(paths, palette)
            }
        }

        drawPath(paths.cheek, palette.blush.copy(alpha = 0.25f))
        mirrored { drawPath(paths.cheek, palette.blush.copy(alpha = 0.25f)) }

        inked(paths.paw, palette.furLight, palette.line)
        mirrored { inked(paths.paw, palette.furLight, palette.line) }
    }
}

/** Fill + crisp anime outline in the virtual space. */
private fun DrawScope.inked(path: Path, fill: Color, line: Color, width: Float = 3f) {
    drawPath(path, fill)
    drawPath(path, line, style = Stroke(width = width, join = StrokeJoin.Round))
}

/** Repeats a draw block mirrored across the fox's vertical centre line (x = 100). */
private fun DrawScope.mirrored(block: DrawScope.() -> Unit) {
    withTransform({ scale(-1f, 1f, pivot = Offset(100f, 0f)) }) { block() }
}

private fun DrawScope.ear(paths: KitsuPaths, palette: KitsuPalette) {
    inked(paths.ear, palette.fur, palette.line)
    inked(paths.earInner, palette.amberDeep, palette.line, width = 2f)
}

private fun DrawScope.drawEye(paths: KitsuPaths, palette: KitsuPalette) {
    drawPath(paths.eye, palette.eye)
    drawPath(paths.eyeLid, palette.line, style = Stroke(2.6f, cap = StrokeCap.Round))
    drawCircle(Color.White, radius = 3.4f, center = Offset(75.5f, 81f))
    drawCircle(Color.White.copy(alpha = 0.8f), radius = 1.6f, center = Offset(81.5f, 87.5f))
}
