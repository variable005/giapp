package com.example.gitpulse.ui.companion

import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the deformation field is exact at rest, keeps the background
 * still, and moves the right regions — the invariants that keep the
 * companion art free of seams on device.
 */
class CompanionWarpMathTest {

    private val out = FloatArray(2)

    private fun math(
        breath: Float = 0f,
        body: Float = 0f,
        tail: Float = 0f,
        head: Float = 0f,
        earL: Float = 0f,
        earR: Float = 0f,
        hop: Float = 0f,
        rise: Float = 0f
    ) = CompanionWarpMath().apply {
        update(breath, body, tail, head, earL, earR, hop, rise)
    }

    @Test
    fun `rest pose is the identity`() {
        val m = CompanionWarpMath()
        m.update(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        m.offsetInto(out, 700f, 900f, 1f)
        assertEquals(0f, out[0], 1e-4f)
        assertEquals(0f, out[1], 1e-4f)
    }

    @Test
    fun `background never moves`() {
        val m = math(breath = 0.8f, body = 3f, tail = 8f, head = 2f, earL = 3f, earR = -3f, hop = 40f)
        m.offsetInto(out, 650f, 880f, 0f)
        assertEquals(0f, out[0], 1e-4f)
        assertEquals(0f, out[1], 1e-4f)
    }

    @Test
    fun `hop lifts the whole body straight up`() {
        val m = math(hop = 25f)
        m.offsetInto(out, 420f, 900f, 1f)
        assertEquals(0f, out[0], 1e-3f)
        assertEquals(-25f, out[1], 1e-3f)
        // the artwork contact point lifts with the body (the shadow sells it)
        m.offsetInto(out, 420f, 1085f, 1f)
        assertEquals(-25f, out[1], 1e-3f)
    }

    @Test
    fun `tail bends around its base and leaves the body alone`() {
        val m = math(tail = -8f)
        // far tail point swings a lot (values pinned to the offline prototype)
        m.offsetInto(out, 950f, 1050f, 1f)
        assertEquals(25.76f, out[0], 0.1f)
        assertEquals(-41.66f, out[1], 0.1f)
        // a body point is untouched by the tail field
        m.offsetInto(out, 300f, 1000f, 1f)
        assertTrue("body must not follow the tail", hypot(out[0], out[1]) < 1.5f)
    }

    @Test
    fun `breath squashes toward the ground line`() {
        val m = math(breath = 1f)
        m.offsetInto(out, 420f, 500f, 1f)
        assertEquals(-5.85f, out[1], 0.05f)
        assertEquals(0f, out[0], 1e-3f)
    }

    @Test
    fun `ears flex without disturbing the tail`() {
        val m = math(earL = 3f, earR = -3f)
        // left ear tip (around 245, 90) moves (value pinned to the prototype)
        m.offsetInto(out, 245f, 90f, 1f)
        assertEquals(11.25f, out[0], 0.1f)
        // tail region untouched by the ear fields
        m.offsetInto(out, 1000f, 900f, 1f)
        assertTrue("tail must not follow the ears", hypot(out[0], out[1]) < 1f)
    }
}
