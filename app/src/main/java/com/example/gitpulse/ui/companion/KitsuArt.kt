package com.example.gitpulse.ui.companion

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.gitpulse.theme.PrimaryEmerald
import com.example.gitpulse.theme.TertiaryAmber
import kotlin.math.roundToInt

/**
 * Kitsu companion art (v3, hand-authored vector fox).
 *
 * All fox geometry lives in a fixed 200 x 185 virtual space so the Paths can be
 * built once and reused under a single scene transform. Stroke widths are
 * expressed in that same virtual space and scale with the fox.
 *
 * v3 highlights: layered cel-shaded fur (body shade, chest ruff, rim light),
 * a kitsune tail fan that grows with the longest streak (1-3 tails), almond
 * anime eyes with emerald irises, a forehead diamond, dark-tipped ears and a
 * pair of expressions (idle / happy) that the hero crossfades between.
 *
 * The scene (sky decor, rolling field, contribution meadow) is drawn in
 * canvas pixel space so it stretches with the screen while the fox keeps its
 * proportions. The scene paints no sky: it sits directly on the screen
 * background, full bleed, as if it were part of the app itself.
 */

/** Paths for one idle chibi fox, in the 200 x 185 virtual space. */
class KitsuPaths {
    val tail = Path().apply {
        moveTo(84f, 152f)
        cubicTo(54f, 158f, 26f, 134f, 26f, 98f)
        cubicTo(27f, 76f, 36f, 64f, 46f, 60f)
    }
    val tailTip = Path().apply {
        moveTo(26f, 98f)
        cubicTo(27f, 76f, 36f, 64f, 46f, 60f)
    }
    val tailShade = Path().apply {
        moveTo(80f, 146f)
        cubicTo(56f, 150f, 34f, 130f, 34f, 106f)
    }

    val body = Path().apply {
        moveTo(100f, 110f)
        cubicTo(84f, 110f, 72f, 122f, 69f, 141f)
        cubicTo(66f, 158f, 78f, 171f, 100f, 171f)
        cubicTo(122f, 171f, 134f, 158f, 131f, 141f)
        cubicTo(128f, 122f, 116f, 110f, 100f, 110f)
        close()
    }
    val bodyShade = Path().apply {
        moveTo(126f, 118f)
        cubicTo(132f, 126f, 134f, 142f, 130f, 154f)
        cubicTo(128f, 163f, 122f, 168f, 114f, 170f)
        cubicTo(122f, 160f, 126f, 146f, 124f, 130f)
        cubicTo(123f, 124f, 124f, 121f, 126f, 118f)
        close()
    }
    val chest = Path().apply {
        moveTo(78f, 131f)
        cubicTo(82f, 122f, 91f, 117f, 100f, 117f)
        cubicTo(109f, 117f, 118f, 122f, 122f, 131f)
        cubicTo(118f, 135f, 113f, 136f, 109f, 134f)
        cubicTo(107f, 139f, 103f, 141f, 100f, 141f)
        cubicTo(97f, 141f, 93f, 139f, 91f, 134f)
        cubicTo(87f, 136f, 82f, 135f, 78f, 131f)
        close()
    }
    val neckShade = Path().apply {
        moveTo(70f, 113f)
        cubicTo(84f, 124f, 116f, 124f, 130f, 113f)
        cubicTo(124f, 121f, 112f, 127f, 100f, 127f)
        cubicTo(88f, 127f, 76f, 121f, 70f, 113f)
        close()
    }

    /** Head with cheek fluff scallops; drawn over the body and ear bases. */
    val head = Path().apply {
        moveTo(58f, 96f)
        cubicTo(50f, 78f, 54f, 54f, 72f, 43f)
        cubicTo(85f, 35f, 115f, 35f, 128f, 43f)
        cubicTo(146f, 54f, 150f, 78f, 142f, 96f)
        cubicTo(138f, 105f, 135f, 108f, 131f, 110f)
        cubicTo(134f, 116f, 130f, 122f, 123f, 124f)
        cubicTo(118f, 130f, 110f, 134f, 100f, 134f)
        cubicTo(90f, 134f, 82f, 130f, 77f, 124f)
        cubicTo(70f, 122f, 66f, 116f, 69f, 110f)
        cubicTo(65f, 108f, 62f, 105f, 58f, 96f)
        close()
    }

    val ear = Path().apply {
        moveTo(62f, 76f)
        cubicTo(56f, 50f, 62f, 26f, 73f, 17f)
        cubicTo(87f, 27f, 96f, 46f, 98f, 64f)
        cubicTo(86f, 72f, 72f, 76f, 62f, 76f)
        close()
    }
    val earInner = Path().apply {
        moveTo(71f, 66f)
        cubicTo(70f, 48f, 73f, 33f, 77f, 24f)
        cubicTo(84f, 33f, 89f, 46f, 91f, 57f)
        cubicTo(84f, 63f, 77f, 66f, 71f, 66f)
        close()
    }

    /** Little crest curl between the ears. */
    val crest = Path().apply {
        moveTo(96f, 40f)
        cubicTo(93f, 31f, 96f, 25f, 104f, 22f)
    }

    /** Rim light running along the top of the skull. */
    val rim = Path().apply {
        moveTo(70f, 52f)
        cubicTo(84f, 40f, 116f, 40f, 130f, 52f)
    }

    /** Kitsune mark on the forehead. */
    val diamondUp = Path().apply {
        moveTo(100f, 49f)
        lineTo(104.5f, 55f)
        lineTo(100f, 61f)
        lineTo(95.5f, 55f)
        close()
    }

    /** Almond eye, open. */
    val eyeOpen = Path().apply {
        moveTo(64f, 86f)
        cubicTo(64f, 75f, 73f, 69f, 81f, 73f)
        cubicTo(89f, 77f, 88f, 86f, 80f, 90f)
        cubicTo(73f, 93f, 66f, 91f, 64f, 86f)
        close()
    }
    val lash = Path().apply {
        moveTo(64.5f, 85.5f)
        cubicTo(65f, 75f, 73f, 69.5f, 81f, 73.5f)
    }

    /** Content `^ ^` eye used by the happy expression. */
    val eyeHappy = Path().apply {
        moveTo(65f, 84f)
        quadraticBezierTo(76f, 93f, 87f, 84f)
    }

    val muzzle = Path().apply { addOval(Rect(80f, 97f, 120f, 119f)) }
    val nose = Path().apply {
        moveTo(95.5f, 97f)
        cubicTo(97f, 94.5f, 103f, 94.5f, 104.5f, 97f)
        cubicTo(103.5f, 101f, 101.5f, 102.5f, 100f, 102.5f)
        cubicTo(98.5f, 102.5f, 96.5f, 101f, 95.5f, 97f)
        close()
    }
    val mouthIdle = Path().apply {
        moveTo(93.5f, 105f)
        quadraticBezierTo(100f, 109.5f, 106.5f, 105f)
    }

    /** Open smile used by the happy expression (filled + tongue). */
    val mouthHappy = Path().apply {
        moveTo(93f, 104f)
        quadraticBezierTo(100f, 112f, 107f, 104f)
        close()
    }

    val blush = Path().apply { addOval(Rect(58.5f, 99f, 71.5f, 108f)) }

    val scarf = Path().apply {
        moveTo(66f, 118f)
        cubicTo(84f, 132f, 116f, 132f, 134f, 118f)
        cubicTo(134f, 128f, 133f, 134f, 131f, 138f)
        cubicTo(112f, 150f, 88f, 150f, 69f, 138f)
        cubicTo(67f, 134f, 66f, 128f, 66f, 118f)
        close()
    }

    /** Chest gem, echoing the forehead diamond. */
    val gem = Path().apply {
        moveTo(100f, 135.5f)
        lineTo(105f, 140.5f)
        lineTo(100f, 145.5f)
        lineTo(95f, 140.5f)
        close()
    }

    val paw = Path().apply { addOval(Rect(72f, 157f, 95f, 174f)) }
}

/** Live motion values driven by the idle loop and tap reactions. */
data class KitsuMotion(
    val blink: Float,
    val breath: Float,
    val tailSway: Float,
    val ahogeSway: Float,
    val earTwitch: Float = 0f,
    /** 0 -> idle face, 1 -> happy face (ears perk, eyes squeeze, mouth opens). */
    val happy: Float = 0f,
    /** Whole-body lean in degrees, used for the curious long-press pose. */
    val tilt: Float = 0f,
    /** Vertical lift in virtual units for the tap hop. */
    val hop: Float = 0f
)

/** Palette for the scene and the fox. Character tones are brand-locked; the
 *  scenery follows the active Material color scheme. */
class KitsuPalette(
    val line: Color,
    val fur: Color,
    val furLight: Color,
    val amber: Color,
    val amberDeep: Color,
    val earTip: Color,
    val eye: Color,
    val nose: Color,
    val blush: Color,
    val tongue: Color,
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
    earTip = Color(0xFFD98A45),
    eye = Color(0xFF2E2A38),
    nose = Color(0xFF4A3830),
    blush = Color(0xFFEE9A6B),
    tongue = Color(0xFFE58A8A),
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

/** Soft second fur tone used for the neck shadow, body shade and tail shading. */
private val FurShade = Color(0xFFEAD3AE)

/** Tail fan geometry per evolution stage: base angle (degrees) and a small
 *  base offset so the tails read as individual ribbons instead of a slab. */
private val TailAngles = mapOf(
    1 to listOf(-8f),
    2 to listOf(-16f, 6f),
    3 to listOf(-24f, -8f, 8f)
)
private val TailOffsets = mapOf(
    1 to listOf(0f),
    2 to listOf(-3f, 2f),
    3 to listOf(-4f, 0f, 4f)
)

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
 * and the companion on the field crest. No sky is painted; the scene
 * blends straight into the screen background.
 *
 * [sunUp] adds a small sun instead of the moon; [tails] is the evolution
 * stage from the longest streak. [companion] is the illustration art drawn
 * on the crest (bottom edge on the ground line); when it is null the
 * hand-drawn vector fox is used unless [foxVisible] is off. The crest
 * keeps its contact shadow either way.
 */
fun DrawScope.drawKitsuScene(
    paths: KitsuPaths,
    palette: KitsuPalette,
    motion: KitsuMotion,
    nightDecor: Boolean,
    sunUp: Boolean,
    tails: Int,
    levels: List<Int>,
    foxVisible: Boolean = true,
    companion: ImageBitmap? = null
) {
    val w = size.width
    val h = size.height

    // Celestial body: sun by day, moon otherwise.
    val celestial = Offset(0.84f * w, 0.16f * h)
    if (sunUp) {
        drawCircle(palette.amber.copy(alpha = 0.25f), 15.dp.toPx(), celestial)
        drawCircle(palette.amber, 9.dp.toPx(), celestial)
    } else {
        drawCircle(palette.furLight, 9.dp.toPx(), celestial)
        drawCircle(
            palette.line.copy(alpha = 0.18f), 2.2.dp.toPx(),
            Offset(celestial.x - 3.dp.toPx(), celestial.y - 3.dp.toPx())
        )
        drawCircle(
            palette.line.copy(alpha = 0.18f), 1.4.dp.toPx(),
            Offset(celestial.x + 3.dp.toPx(), celestial.y + 3.dp.toPx())
        )
    }

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

    // Companion: the illustration art sits where the fox stands — bottom edge
    // on the crest, centred. Idle breath bob, plus the vector fox's reaction
    // vocabulary in art scale: tap hop, happy rise and the curious lean.
    if (companion != null) {
        val artHeight = 152.dp.toPx()
        val artWidth = artHeight * companion.width / companion.height
        val groundLineY = groundCenter + 5.dp.toPx()
        val bob = motion.breath * 2.dp.toPx()
        val lift = motion.hop * artHeight / 185f + motion.happy * 3.dp.toPx()
        withTransform({
            rotate(motion.tilt, pivot = Offset(w / 2f, groundLineY))
        }) {
            drawImage(
                image = companion,
                dstOffset = IntOffset(
                    (w / 2f - artWidth / 2f).roundToInt(),
                    (groundLineY - artHeight - bob - lift).roundToInt()
                ),
                dstSize = IntSize(artWidth.roundToInt(), artHeight.roundToInt())
            )
        }
    } else if (foxVisible) {
        val foxScale = 145.dp.toPx() / 185f
        val foxTx = w / 2f - 100f * foxScale
        val foxTy = groundCenter + 5.dp.toPx() - 185f * foxScale
        withTransform({
            translate(foxTx, foxTy)
            scale(foxScale, foxScale, pivot = Offset.Zero)
        }) {
            drawKitsu(paths, palette, motion, tails)
        }
    }
}

/** A small filled heart used for the tap celebration burst. */
fun DrawScope.drawCompanionHeart(center: Offset, radius: Float, color: Color) {
    val heart = Path().apply {
        moveTo(center.x, center.y + radius * 0.9f)
        cubicTo(
            center.x - radius * 1.25f, center.y - radius * 0.45f,
            center.x - radius * 0.7f, center.y - radius * 1.3f,
            center.x, center.y - radius * 0.55f
        )
        cubicTo(
            center.x + radius * 0.7f, center.y - radius * 1.3f,
            center.x + radius * 1.25f, center.y - radius * 0.45f,
            center.x, center.y + radius * 0.9f
        )
        close()
    }
    drawPath(heart, color)
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

private fun DrawScope.drawKitsu(
    paths: KitsuPaths,
    palette: KitsuPalette,
    motion: KitsuMotion,
    tails: Int
) {
    // Kitsune tail fan. Each tail sways around its own base with a slightly
    // different phase so the fan feels alive.
    val stage = tails.coerceIn(1, 3)
    val angles = TailAngles.getValue(stage)
    val offsets = TailOffsets.getValue(stage)
    angles.forEachIndexed { i, angle ->
        val dx = offsets.getOrElse(i) { 0f }
        val sway = motion.tailSway * (1f - i * 0.12f)
        withTransform({ rotate(degrees = angle + sway, pivot = Offset(84f + dx, 152f)) }) {
            drawPath(paths.tail, palette.line, style = Stroke(30f, cap = StrokeCap.Round))
            drawPath(paths.tail, palette.fur, style = Stroke(25f, cap = StrokeCap.Round))
            drawPath(paths.tailTip, palette.line, style = Stroke(30f, cap = StrokeCap.Round))
            drawPath(paths.tailTip, palette.amber, style = Stroke(25f, cap = StrokeCap.Round))
            drawPath(paths.tailShade, FurShade.copy(alpha = 0.75f), style = Stroke(6f, cap = StrokeCap.Round))
        }
    }

    withTransform({
        translate(0f, -motion.hop)
        rotate(degrees = motion.tilt, pivot = Offset(100f, 173f))
        // Breathing subtly stretches the whole sprite from the ground up.
        scale(1f, 1f + motion.breath * 0.012f, pivot = Offset(100f, 185f))
    }) {
        // Body, cel shade and chest ruff.
        inked(paths.body, palette.fur, palette.line)
        drawPath(paths.bodyShade, FurShade.copy(alpha = 0.45f))
        drawPath(paths.chest, palette.furLight)

        // Front paws with toe notches.
        inked(paths.paw, palette.furLight, palette.line)
        drawToes(palette)
        mirrored {
            inked(paths.paw, palette.furLight, palette.line)
            drawToes(palette)
        }

        // Scarf with its emerald gem.
        drawPath(paths.neckShade, FurShade.copy(alpha = 0.8f))
        inked(paths.scarf, palette.emerald, palette.line)
        drawPath(paths.gem, palette.emeraldDeep)
        drawPath(paths.gem, palette.line, style = Stroke(1.7f))
        drawCircle(Color.White.copy(alpha = 0.5f), radius = 1.1f, center = Offset(98.2f, 138.6f))

        // Ears perk outward when happy and twitch during idle.
        val earRotation = -6f * motion.happy + motion.earTwitch
        withTransform({ rotate(degrees = earRotation, pivot = Offset(62f, 76f)) }) {
            ear(paths, palette)
            mirrored { ear(paths, palette) }
        }

        // Crest curl between the ears.
        withTransform({ rotate(degrees = motion.ahogeSway, pivot = Offset(102f, 42f)) }) {
            drawPath(paths.crest, palette.line, style = Stroke(8f, cap = StrokeCap.Round))
            drawPath(paths.crest, palette.fur, style = Stroke(5f, cap = StrokeCap.Round))
        }

        // Head with rim light and kitsune mark.
        inked(paths.head, palette.fur, palette.line)
        drawPath(paths.rim, palette.furLight.copy(alpha = 0.8f), style = Stroke(5f, cap = StrokeCap.Round))
        drawPath(paths.diamondUp, palette.emerald)
        drawPath(paths.diamondUp, palette.emeraldDeep, style = Stroke(1.6f))

        // Eyes crossfade between the open almond pair (which blinks) and the
        // happy arcs.
        val openAlpha = 1f - motion.happy
        if (openAlpha > 0.01f) {
            withTransform({ scale(1f, motion.blink, pivot = Offset(76f, 83f)) }) {
                drawEyeOpen(paths, palette, openAlpha)
            }
            mirrored {
                withTransform({ scale(1f, motion.blink, pivot = Offset(76f, 83f)) }) {
                    drawEyeOpen(paths, palette, openAlpha)
                }
            }
        }
        if (motion.happy > 0.01f) {
            drawPath(
                paths.eyeHappy, palette.eye.copy(alpha = motion.happy),
                style = Stroke(3.2f, cap = StrokeCap.Round)
            )
            mirrored {
                drawPath(
                    paths.eyeHappy, palette.eye.copy(alpha = motion.happy),
                    style = Stroke(3.2f, cap = StrokeCap.Round)
                )
            }
        }

        // Muzzle, nose, mouth.
        drawPath(paths.muzzle, palette.furLight)
        drawPath(paths.muzzle, palette.line.copy(alpha = 0.3f), style = Stroke(1.6f))
        drawPath(paths.nose, palette.nose)
        if (motion.happy > 0.5f) {
            drawPath(paths.mouthHappy, palette.nose)
            drawCircle(palette.tongue, radius = 2.1f, center = Offset(100f, 106.8f))
        } else {
            drawPath(paths.mouthIdle, palette.nose, style = Stroke(2.2f, cap = StrokeCap.Round))
        }

        // Cheek blush.
        drawPath(paths.blush, palette.blush.copy(alpha = 0.3f))
        mirrored { drawPath(paths.blush, palette.blush.copy(alpha = 0.3f)) }
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

private fun DrawScope.drawToes(palette: KitsuPalette) {
    val toeColor = palette.line.copy(alpha = 0.55f)
    drawLine(toeColor, Offset(80f, 160f), Offset(80f, 168f), strokeWidth = 1.6f, cap = StrokeCap.Round)
    drawLine(toeColor, Offset(87f, 160f), Offset(87f, 168f), strokeWidth = 1.6f, cap = StrokeCap.Round)
}

private fun DrawScope.ear(paths: KitsuPaths, palette: KitsuPalette) {
    drawPath(paths.ear, palette.fur)
    // Dark fur tip, clipped to the ear silhouette.
    clipPath(paths.ear) {
        drawCircle(palette.earTip, radius = 27f, center = Offset(78f, 16f))
    }
    drawPath(paths.ear, palette.line, style = Stroke(3f, join = StrokeJoin.Round))
    drawPath(paths.earInner, palette.amberDeep)
    drawPath(paths.earInner, palette.line, style = Stroke(2f, join = StrokeJoin.Round))
}

private fun DrawScope.drawEyeOpen(paths: KitsuPaths, palette: KitsuPalette, alpha: Float) {
    drawPath(paths.eyeOpen, palette.eye.copy(alpha = alpha))
    drawCircle(palette.emeraldDeep.copy(alpha = alpha), radius = 4.4f, center = Offset(76f, 83.5f))
    drawCircle(palette.eye.copy(alpha = alpha), radius = 2.5f, center = Offset(76f, 83.8f))
    drawCircle(Color.White.copy(alpha = alpha), radius = 2.1f, center = Offset(73.2f, 80.6f))
    drawCircle(Color.White.copy(alpha = 0.75f * alpha), radius = 1f, center = Offset(80.2f, 87f))
    drawPath(paths.lash, palette.line.copy(alpha = alpha), style = Stroke(2.6f, cap = StrokeCap.Round))
}
