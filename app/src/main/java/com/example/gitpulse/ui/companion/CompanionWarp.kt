package com.example.gitpulse.ui.companion

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.hypot

private const val DEG = 0.017453292f

/**
 * Pure deformation field for the companion art — no Android dependencies so
 * the whole rig can be unit tested on the JVM.
 *
 * The artwork is treated as a rubber sheet: smooth influence fields bend the
 * tail around its base, sway the body about the ground line, nod the head,
 * flex the ears and squash the body with the breath. The ear and tail gates
 * fade into each other (partition of unity), so no field boundary ever
 * shears the drawn strokes. [a] is the silhouette alpha at the sample point:
 * background samples never move, so the transparent margin stays put.
 *
 * All coordinates are in art pixels (the 1170 x 1099 source image).
 */
internal class CompanionWarpMath {

    private var breath = 0f
    private var hopPx = 0f
    private var risePx = 0f
    private var shiftPx = 0f
    private var headShiftPx = 0f
    private var headLiftPx = 0f
    private var squash = 0f

    // cached trig, refreshed once per frame in [update]
    private var cB = 1f; private var sB = 0f
    private var cT = 1f; private var sT = 0f
    private var cH = 1f; private var sH = 0f
    private var cEL = 1f; private var sEL = 0f
    private var cER = 1f; private var sER = 0f

    fun update(
        breath: Float,
        bodyAngleDeg: Float,
        tailAngleDeg: Float,
        headAngleDeg: Float,
        earLDeg: Float,
        earRDeg: Float,
        hopPx: Float,
        risePx: Float,
        shiftPx: Float = 0f,
        headShiftPx: Float = 0f,
        headLiftPx: Float = 0f,
        squash: Float = 0f
    ) {
        this.breath = breath
        this.hopPx = hopPx
        this.risePx = risePx
        this.shiftPx = shiftPx
        this.headShiftPx = headShiftPx
        this.headLiftPx = headLiftPx
        this.squash = squash
        cB = cosDeg(bodyAngleDeg); sB = sinDeg(bodyAngleDeg)
        cT = cosDeg(tailAngleDeg); sT = sinDeg(tailAngleDeg)
        cH = cosDeg(headAngleDeg); sH = sinDeg(headAngleDeg)
        cEL = cosDeg(earLDeg); sEL = sinDeg(earLDeg)
        cER = cosDeg(earRDeg); sER = sinDeg(earRDeg)
    }

    /** Writes the displacement of art point (x, y) into out[0] (dx), out[1] (dy). */
    fun offsetInto(out: FloatArray, x: Float, y: Float, a: Float) {
        if (a <= 0f) {
            out[0] = 0f
            out[1] = 0f
            return
        }

        // Influence fields.
        val wEarL = ss(50f, 210f, hypot(x - 245f, y - 305f)) * ss(360f, 280f, y) * a
        val wEarR = ss(60f, 240f, hypot(x - 565f, y - 510f)) *
            ss(620f, 470f, y) * ss(520f, 580f, x) * a
        val wHead = ss(660f, 470f, y) * ss(800f, 650f, x) * a *
            (1f - wEarL) * (1f - wEarR)
        val wTail = ss(90f, 330f, hypot(x - 665f, y - 845f)) *
            ss(340f, 450f, y) * ss(580f, 700f, x) * a *
            (1f - wEarR) * (1f - wHead)

        var dx = 0f
        var dy = 0f

        // Body: lean about the ground line, breath and landing squash, hop,
        // happy rise and the lateral weight shift.
        dx += a * rotX(cB, sB, x, y, 420f, 1085f)
        dy += a * rotY(cB, sB, x, y, 420f, 1085f)
        val s = 1f + breath * 0.010f + squash * 0.085f
        dx += a * (x - 420f) * (-0.5f * (s - 1f))
        dy += a * (y - 1085f) * (s - 1f)
        dy -= a * (hopPx + risePx)
        dx += a * shiftPx

        // Tail: bends around its base (665, 845).
        dx += wTail * rotX(cT, sT, x, y, 665f, 845f)
        dy += wTail * rotY(cT, sT, x, y, 665f, 845f)

        // Head: nods around the neck (315, 650), and can also travel a little
        // sideways and up, which is what reads as her looking around.
        dx += wHead * rotX(cH, sH, x, y, 315f, 650f)
        dy += wHead * rotY(cH, sH, x, y, 315f, 650f)
        dx += wHead * headShiftPx
        dy -= wHead * headLiftPx

        // Ears: flex at their bases.
        dx += wEarL * rotX(cEL, sEL, x, y, 245f, 305f)
        dy += wEarL * rotY(cEL, sEL, x, y, 245f, 305f)
        dx += wEarR * rotX(cER, sER, x, y, 565f, 510f)
        dy += wEarR * rotY(cER, sER, x, y, 565f, 510f)

        out[0] = dx
        out[1] = dy
    }

    private fun ss(e0: Float, e1: Float, v: Float): Float {
        val t = ((v - e0) / (e1 - e0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun rotX(c: Float, s: Float, x: Float, y: Float, px: Float, py: Float): Float =
        (x - px) * (c - 1f) - (y - py) * s

    private fun rotY(c: Float, s: Float, x: Float, y: Float, px: Float, py: Float): Float =
        (x - px) * s + (y - py) * (c - 1f)

    private fun cosDeg(deg: Float): Float = kotlin.math.cos(deg * DEG)
    private fun sinDeg(deg: Float): Float = kotlin.math.sin(deg * DEG)
}

/**
 * Draws the companion art through a GPU mesh (Canvas.drawBitmapMesh): each
 * frame the bitmap is warped by [CompanionWarpMath] so the tail bends, the
 * body breathes and the ears flex — the pixels are never redrawn.
 *
 * Setup builds one padded copy of the art (transparent motion margin) and a
 * small silhouette-alpha grid that feeds the influence fields. All of it is
 * cheap one-time work; drawing is a handful of float writes per frame.
 *
 * Retired from the hero: the companion is now animated by the hand-drawn
 * frame sets in [KitsuFrames] (see [drawKitsuScene]), so nothing in the app
 * instantiates this rig anymore. It is kept around as the generic mesh-warp
 * implementation, and [CompanionWarpMath] remains covered by
 * CompanionWarpMathTest.
 */
class CompanionWarp(image: ImageBitmap, private val cells: Int = 32) {

    private companion object {
        /** Art px of transparent margin so bent pixels have room to move into. */
        const val PAD = 130

        /** Resolution of the silhouette alpha grid. */
        const val MASK_N = 64

        /** Ground line in art px. */
        const val GROUND_Y = 1085f
    }

    private val padded: Bitmap
    private val mask: FloatArray

    private var wArt = 0f
    private var hArt = 0f

    private val verts = FloatArray((cells + 1) * (cells + 1) * 2)
    private val off = FloatArray(2)
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val math = CompanionWarpMath()

    init {
        val src = image.asAndroidBitmap()
        wArt = src.width.toFloat()
        hArt = src.height.toFloat()
        val p = Bitmap.createBitmap(
            src.width + 2 * PAD, src.height + 2 * PAD, Bitmap.Config.ARGB_8888
        )
        AndroidCanvas(p).drawBitmap(src, PAD.toFloat(), PAD.toFloat(), null)
        padded = p

        // silhouette alpha grid (downscaled copy, one pixel read)
        val n2 = MASK_N * 2
        val small = Bitmap.createScaledBitmap(p, n2, n2, true)
        val px = IntArray(n2 * n2)
        small.getPixels(px, 0, n2, 0, 0, n2, n2)
        small.recycle()
        val m = FloatArray((MASK_N + 1) * (MASK_N + 1))
        val sx = n2.toFloat() / p.width
        val sy = n2.toFloat() / p.height
        for (j in 0..MASK_N) {
            val py = ((PAD + j.toFloat() / MASK_N * hArt) * sy).toInt().coerceIn(0, n2 - 1)
            for (i in 0..MASK_N) {
                val pxx = ((PAD + i.toFloat() / MASK_N * wArt) * sx).toInt().coerceIn(0, n2 - 1)
                m[j * (MASK_N + 1) + i] = ((px[py * n2 + pxx] ushr 24) and 0xFF) / 255f
            }
        }
        mask = m
    }

    /**
     * Warps and draws the companion for this frame.
     *
     * @return true when drawn; false signals the caller to use its static
     * fallback path instead.
     */
    fun draw(
        scope: DrawScope,
        motion: KitsuMotion,
        centerX: Float,
        groundY: Float,
        heightPx: Float
    ): Boolean {
        val scale = heightPx / hArt
        math.update(
            breath = motion.breath,
            bodyAngleDeg = motion.tilt + motion.lean + motion.sway * 0.9f,
            tailAngleDeg = motion.tailSway * 2f + motion.tailWhip + motion.happy * 1.5f,
            headAngleDeg = (0.5f - motion.breath) * 2f - motion.happy * 1.5f + motion.headTurn,
            earLDeg = motion.earTwitch * 0.5f + motion.earLag - motion.happy * 1.2f,
            earRDeg = -motion.earTwitch * 0.4f + motion.earLag * 0.85f + motion.happy * 1f,
            hopPx = motion.hop * hArt / 185f,
            risePx = motion.happy * 3f * scope.density / scale,
            shiftPx = motion.shift * hArt / 185f,
            headShiftPx = motion.headShift * hArt / 185f,
            headLiftPx = motion.headLift * hArt / 185f,
            squash = motion.squash
        )

        val padW = wArt + 2 * PAD
        val padH = hArt + 2 * PAD
        val left = centerX - padW / 2f * scale
        val top = groundY - (GROUND_Y + PAD) * scale
        var idx = 0
        for (j in 0..cells) {
            val v = j.toFloat() / cells
            val rowY = top + v * padH * scale
            val yArt = v * padH - PAD
            for (i in 0..cells) {
                val u = i.toFloat() / cells
                val colX = left + u * padW * scale
                val xArt = u * padW - PAD
                val a = maskAt(xArt, yArt)
                math.offsetInto(off, xArt, yArt, a)
                verts[idx++] = colX + off[0] * scale
                verts[idx++] = rowY + off[1] * scale
            }
        }

        scope.drawIntoCanvas { c ->
            c.nativeCanvas.drawBitmapMesh(padded, cells, cells, verts, 0, null, 0, paint)
        }
        return true
    }

    private fun maskAt(x: Float, y: Float): Float {
        if (x < 0f || y < 0f || x > wArt || y > hArt) return 0f
        val gx = x / wArt * MASK_N
        val gy = y / hArt * MASK_N
        val x0 = gx.toInt().coerceIn(0, MASK_N - 1)
        val y0 = gy.toInt().coerceIn(0, MASK_N - 1)
        val fx = gx - x0
        val fy = gy - y0
        val row = y0 * (MASK_N + 1)
        val row1 = row + (MASK_N + 1)
        val m00 = mask[row + x0]
        val m10 = mask[row + x0 + 1]
        val m01 = mask[row1 + x0]
        val m11 = mask[row1 + x0 + 1]
        return (m00 * (1f - fx) + m10 * fx) * (1f - fy) +
            (m01 * (1f - fx) + m11 * fx) * fy
    }
}
