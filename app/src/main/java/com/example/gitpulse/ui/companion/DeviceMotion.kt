package com.example.gitpulse.ui.companion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * How the fox answers the phone itself.
 *
 * Values are already in the units [KitsuMotion] wants, so the composer only
 * has to mix them in. The accelerometer gives two useful signals at once: the
 * low-passed gravity vector (which way the phone is leaning) and the residual
 * (how much the hand is moving).
 *
 * Registered only while the hero is on screen and the app is resumed, so
 * nothing runs in the background.
 */
@Stable
internal class DeviceMotion {
    /** Body lean in degrees — tilt the phone, she tips with it. */
    var lean by mutableFloatStateOf(0f)

    /** Head yaw in degrees, relative to the body, trailing behind it. */
    var headTurn by mutableFloatStateOf(0f)

    /** Ear degrees, relative to the head, trailing behind it. */
    var earLag by mutableFloatStateOf(0f)

    /** Whole-body lateral offset in virtual units — her weight transferring. */
    var shift by mutableFloatStateOf(0f)

    /** Head rise in virtual units, from tilting the phone back. */
    var headLift by mutableFloatStateOf(0f)

    /** Tail whip in degrees, trailing the body swing. */
    var tailWhip by mutableFloatStateOf(0f)

    /** Damped bounce kicked by jostling; drives the hop and the squash. */
    var bounce by mutableFloatStateOf(0f)

    /** 0..1 hand movement, smoothed. */
    var energy by mutableFloatStateOf(0f)

    /** 0..1 slow "paying attention" envelope: rises when handled, decays when set down. */
    var arousal by mutableFloatStateOf(0f)

    /** Snaps back to neutral, so the fox never freezes mid-lean. */
    internal fun settle() {
        lean = 0f
        headTurn = 0f
        earLag = 0f
        shift = 0f
        headLift = 0f
        tailWhip = 0f
        bounce = 0f
        energy = 0f
        arousal = 0f
    }
}

/**
 * A first-order spring, used as a lag filter.
 *
 * [k] is how fast it chases the target and [d] how hard it damps. Softer
 * springs further down the chain (head, ears, tail) naturally trail the body,
 * which is where the follow-through comes from — no hand-authored keyframes
 * needed. Underdamped values make it settle with a small overshoot, so the
 * motion has weight instead of snapping into place.
 */
private class Lag(private val k: Float, private val d: Float) {

    private var value = 0f
    private var velocity = 0f

    fun next(target: Float, step: Float): Float {
        velocity += ((target - value) * k - velocity * d) * step
        value += velocity * step
        return value
    }

    fun reset() {
        value = 0f
        velocity = 0f
    }
}

/**
 * Splits the accelerometer into "which way is down" and "how much is the hand
 * moving", then drives a chain of springs from it.
 *
 * Everything is normalised by the event rate ([step]) so the feel does not
 * change between devices that report at 15 Hz and ones that report at 100 Hz.
 */
private class GravityListener(private val motion: DeviceMotion) : SensorEventListener {

    private companion object {
        /** Body leads. */
        const val K_BODY = 0.22f
        const val D_BODY = 0.42f

        /** Head trails the body. */
        const val K_HEAD = 0.13f
        const val D_HEAD = 0.40f

        /** Ears trail the head. */
        const val K_EAR = 0.09f
        const val D_EAR = 0.38f

        /** Tail trails everything. */
        const val K_TAIL = 0.07f
        const val D_TAIL = 0.36f

        /** Degrees of lean at full tilt. */
        const val LEAN_DEG = 7f
        const val HEAD_DEG = 10f
        const val EAR_DEG = 12f
        const val TAIL_DEG = 8f

        /** Virtual units of lateral travel at full lean. */
        const val SHIFT_UNITS = 0.55f

        /** Virtual units of head lift at full back-tilt. */
        const val LIFT_UNITS = 3.5f
    }

    // Low-passed gravity, in m/s^2. Seeded with the phone held upright, the
    // usual pose; the filter converges from any start within a fraction of a
    // second either way.
    private var gx = 0f
    private var gy = 9.81f
    private var gz = 0f

    private val body = Lag(K_BODY, D_BODY)
    private val head = Lag(K_HEAD, D_HEAD)
    private val ears = Lag(K_EAR, D_EAR)
    private val tail = Lag(K_TAIL, D_TAIL)

    private var bounceVelocity = 0f
    private var lastTimestamp = 0L

    override fun onSensorChanged(event: SensorEvent) {
        val dt = if (lastTimestamp == 0L) {
            0.02f
        } else {
            ((event.timestamp - lastTimestamp) / 1_000_000_000f).coerceIn(0.002f, 0.05f)
        }
        lastTimestamp = event.timestamp
        // Capped so a slow reporter cannot destabilise the springs.
        val step = (dt / 0.02f).coerceIn(0.4f, 1.5f)

        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        // Slow part is gravity, fast part is the hand.
        val gravBlend = 1f - exp(-6f * dt)
        gx += (ax - gx) * gravBlend
        gy += (ay - gy) * gravBlend
        gz += (az - gz) * gravBlend
        val magnitude = sqrt(gx * gx + gy * gy + gz * gz).coerceAtLeast(0.001f)

        val rx = ax - gx
        val ry = ay - gy
        val rz = az - gz
        val jostle = (sqrt(rx * rx + ry * ry + rz * rz) / 9.81f).coerceAtMost(1.5f)

        val previousEnergy = motion.energy
        motion.energy += (jostle - motion.energy) * (1f - exp(-7f * dt))

        // Picking her up gets her attention; putting the phone down lets her
        // settle again, much more slowly than she woke.
        val awake = if (motion.energy > 0.06f) 1f else 0f
        val awakeRate = if (awake > motion.arousal) 3.5f else 0.3f
        motion.arousal += (awake - motion.arousal) * (1f - exp(-awakeRate * dt))

        // Side-to-side tilt straight off the gravity vector. Zero with the
        // phone upright *or* flat, so she sits neutral until you actually tilt.
        val roll = (-gx / magnitude).coerceIn(-1f, 1f)

        // Laying the phone back lifts gz off zero, which reads as leaning over
        // her. Unlike roll this is a posture, not a motion, so it is gated on
        // her attention and clears when the phone is put down.
        val backTilt = (gz / magnitude).coerceIn(0f, 1f)

        // She tips downhill with the tilt. The softer springs below swing
        // through the motion instead of the whole fox moving as one piece.
        val bodyAngle = body.next(roll * LEAN_DEG, step)
        val headAngle = head.next(roll * HEAD_DEG, step)
        val earAngle = ears.next(roll * EAR_DEG, step)
        val tailAngle = tail.next(roll * TAIL_DEG, step)

        // Each joint reports its rotation relative to the one above it, which
        // is what makes the head, ears and tail swing back through the motion.
        motion.lean = bodyAngle
        motion.headTurn = headAngle - bodyAngle
        motion.earLag = earAngle - headAngle
        motion.tailWhip = tailAngle - bodyAngle
        motion.shift = bodyAngle * SHIFT_UNITS
        motion.headLift = backTilt * LIFT_UNITS * motion.arousal

        // A sudden jolt kicks a lightly damped oscillator, so she boings a
        // couple of times and settles instead of just sliding.
        val kick = motion.energy - previousEnergy
        if (kick > 0.015f) bounceVelocity += kick * 2.6f
        bounceVelocity += (0f - motion.bounce) * 0.16f * step - bounceVelocity * 0.20f * step

        val bounced = motion.bounce + bounceVelocity * step
        when {
            bounced > 1.2f -> {
                motion.bounce = 1.2f
                bounceVelocity = 0f
            }
            bounced < -1.2f -> {
                motion.bounce = -1.2f
                bounceVelocity = 0f
            }
            else -> motion.bounce = bounced
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    /** Drops the springs and re-seeds gravity for an upright phone. */
    fun reset() {
        gx = 0f
        gy = 9.81f
        gz = 0f
        body.reset()
        head.reset()
        ears.reset()
        tail.reset()
        bounceVelocity = 0f
        lastTimestamp = 0L
    }
}

/**
 * Remembers the phone-motion channels for as long as [enabled] holds and the
 * host is resumed. Returns a neutral, never-moving value on devices without an
 * accelerometer, and under reduced-motion settings.
 */
@Composable
internal fun rememberDeviceMotion(enabled: Boolean): DeviceMotion {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val motion = remember { DeviceMotion() }

    DisposableEffect(context, lifecycleOwner, enabled) {
        if (!enabled) {
            motion.settle()
            return@DisposableEffect onDispose { }
        }

        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (manager == null || sensor == null) {
            return@DisposableEffect onDispose { }
        }

        val listener = GravityListener(motion)
        var registered = false

        fun start() {
            if (!registered) {
                manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
                registered = true
            }
        }

        fun stop() {
            if (registered) {
                manager.unregisterListener(listener)
                registered = false
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> start()
                Lifecycle.Event.ON_PAUSE -> {
                    stop()
                    // Clear the springs so she is not leaning when the app
                    // comes back a day later.
                    motion.settle()
                    listener.reset()
                }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            start()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stop()
            motion.settle()
        }
    }

    return motion
}
