package com.example.gitpulse.ui.companion

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

/**
 * How much life the fox has in her, per mood.
 *
 * This is what makes the mood reach the body instead of only recolouring a
 * chip: a thriving fox breathes faster and deeper, shifts her weight more and
 * glances around often, while a resting one breathes slow and shallow and
 * mostly holds still.
 *
 * Durations are in milliseconds; depths are multipliers around 1.
 */
internal data class MotionProfile(
    val breathMs: Int,
    val breathDepth: Float,
    val swayMs: Int,
    val swayDepth: Float,
    val tailMs: Int,
    val tailEnergy: Float,
    val blinkMs: Int,
    val lookMs: Int,
    val lookDeg: Float
)

internal fun motionProfile(mood: CompanionMood): MotionProfile = when (mood) {
    CompanionMood.THRIVING -> MotionProfile(
        breathMs = 3400,
        breathDepth = 1f,
        swayMs = 5800,
        swayDepth = 1f,
        tailMs = 2300,
        tailEnergy = 1.3f,
        blinkMs = 3000,
        lookMs = 7000,
        lookDeg = 7f
    )
    CompanionMood.STEADY -> MotionProfile(
        breathMs = 4400,
        breathDepth = 0.78f,
        swayMs = 7400,
        swayDepth = 0.72f,
        tailMs = 3000,
        tailEnergy = 0.95f,
        blinkMs = 4200,
        lookMs = 9400,
        lookDeg = 5.5f
    )
    CompanionMood.RESTING -> MotionProfile(
        breathMs = 6200,
        breathDepth = 0.5f,
        swayMs = 10200,
        swayDepth = 0.4f,
        tailMs = 4300,
        tailEnergy = 0.6f,
        blinkMs = 6000,
        lookMs = 13000,
        lookDeg = 3.2f
    )
}

/** Raw idle channels, before mood, reactions and the phone are folded in. */
internal data class KitsuIdle(
    val blink: Float,
    val breath: Float,
    val sway: Float,
    val tail: Float,
    val earTwitch: Float,
    val look: Float
)

/**
 * The idle loop.
 *
 * Every channel is a [keyframes] timeline rather than a symmetric
 * `RepeatMode.Reverse` tween. That distinction is the whole trick: a tween
 * spends equal time in every phase, which reads as a metronome, while these
 * envelopes hold, then beat, then hold again — the way a real animal sits
 * still and then twitches. Nothing ever fully freezes, so she still reads as
 * continuously alive.
 *
 * With [enabled] off (reduced-motion settings) nothing is created at all, so a
 * still fox costs a still fox.
 */
@Composable
internal fun rememberKitsuIdle(profile: MotionProfile, enabled: Boolean): KitsuIdle {
    if (!enabled) {
        return KitsuIdle(blink = 1f, breath = 0f, sway = 0f, tail = 0f, earTwitch = 0f, look = 0f)
    }

    val transition = rememberInfiniteTransition(label = "kitsu_idle")

    // Breath: long inhale, a beat held at the top, slower exhale.
    val breath by transition.animateFloat(
        0f, 0f,
        infiniteRepeatable(
            keyframes {
                val d = profile.breathMs
                durationMillis = d
                0f at 0
                0.62f at (d * 0.20f).toInt()
                1f at (d * 0.36f).toInt()
                0.92f at (d * 0.52f).toInt()
                1f at (d * 0.62f).toInt()
                0.28f at (d * 0.84f).toInt()
                0f at d
            },
            RepeatMode.Restart
        ),
        label = "breath"
    )

    // Weight shift: leans onto one paw, rests there, swings across, rests.
    val sway by transition.animateFloat(
        0f, 0f,
        infiniteRepeatable(
            keyframes {
                val d = profile.swayMs
                durationMillis = d
                0f at 0
                0.7f at (d * 0.14f).toInt()
                1f at (d * 0.24f).toInt()
                1f at (d * 0.40f).toInt()
                0.2f at (d * 0.54f).toInt()
                -0.85f at (d * 0.70f).toInt()
                -1f at (d * 0.78f).toInt()
                -1f at (d * 0.90f).toInt()
                0f at d
            },
            RepeatMode.Restart
        ),
        label = "sway"
    )

    // Tail: a rolling base with a double flick, then it swings the other way.
    val tail by transition.animateFloat(
        0f, 0f,
        infiniteRepeatable(
            keyframes {
                val d = profile.tailMs
                durationMillis = d
                0f at 0
                0.55f at (d * 0.08f).toInt()
                0.18f at (d * 0.16f).toInt()
                0.5f at (d * 0.24f).toInt()
                0.1f at (d * 0.34f).toInt()
                -0.35f at (d * 0.46f).toInt()
                -0.85f at (d * 0.56f).toInt()
                -0.3f at (d * 0.66f).toInt()
                -0.7f at (d * 0.74f).toInt()
                -0.15f at (d * 0.86f).toInt()
                0f at d
            },
            RepeatMode.Restart
        ),
        label = "tail"
    )

    // Blink: an irregular pair. Two blinks close together is the single most
    // recognisable "this thing is alive" signal there is.
    val blink by transition.animateFloat(
        1f, 1f,
        infiniteRepeatable(
            keyframes {
                val d = profile.blinkMs
                durationMillis = d
                1f at 0
                1f at (d * 0.46f).toInt()
                0.08f at (d * 0.50f).toInt()
                1f at (d * 0.545f).toInt()
                1f at (d * 0.62f).toInt()
                0.08f at (d * 0.66f).toInt()
                1f at (d * 0.705f).toInt()
                1f at d
            },
            RepeatMode.Restart
        ),
        label = "blink"
    )

    // Ear: two flicks of different size, well apart.
    val earTwitch by transition.animateFloat(
        0f, 0f,
        infiniteRepeatable(
            keyframes {
                val d = profile.blinkMs + 880
                durationMillis = d
                0f at 0
                0f at (d * 0.62f).toInt()
                -6.5f at (d * 0.70f).toInt()
                0f at (d * 0.78f).toInt()
                0f at (d * 0.86f).toInt()
                -3.5f at (d * 0.92f).toInt()
                0f at d
            },
            RepeatMode.Restart
        ),
        label = "ear_twitch"
    )

    // Glance: she looks off to one side and holds it, then a quicker look the
    // other way. With rigid art this is the only way she can "look at" you,
    // so it carries a lot of the personality.
    val look by transition.animateFloat(
        0f, 0f,
        infiniteRepeatable(
            keyframes {
                val d = profile.lookMs
                durationMillis = d
                0f at 0
                0f at (d * 0.10f).toInt()
                -1f at (d * 0.20f).toInt()
                -1f at (d * 0.36f).toInt()
                0.18f at (d * 0.48f).toInt()
                0f at (d * 0.56f).toInt()
                0f at (d * 0.72f).toInt()
                1f at (d * 0.82f).toInt()
                1f at (d * 0.90f).toInt()
                0f at d
            },
            RepeatMode.Restart
        ),
        label = "look"
    )

    return KitsuIdle(
        blink = blink,
        breath = breath,
        sway = sway,
        tail = tail,
        earTwitch = earTwitch,
        look = look
    )
}

/** A one-shot pose layered over the idle channels and the phone response. */
internal data class KitsuPose(
    val happy: Float = 0f,
    val tilt: Float = 0f,
    /** Vertical arc in virtual units for the tap hop. */
    val hop: Float = 0f,
    /** Volume-preserving deformation: positive stretches, negative squashes. */
    val squash: Float = 0f
)

/**
 * Folds the idle channels, the mood profile, the phone response and the
 * current pose into one [KitsuMotion].
 *
 * Pure by design so the blend can be reasoned about (and tested) without a
 * running animation: the idle loop supplies rhythm, [profile] sets how much of
 * it the current mood gets, [device] carries how the hand is holding the
 * phone, and [pose] is whatever the last interaction was.
 *
 * When the phone is being handled ([DeviceMotion.arousal] is high) her idle
 * glances step aside, because she is already looking at you.
 */
internal fun kitsuMotion(
    idle: KitsuIdle,
    profile: MotionProfile,
    device: DeviceMotion,
    pose: KitsuPose,
    reducedMotion: Boolean
): KitsuMotion {
    if (reducedMotion) {
        return KitsuMotion(blink = 1f, breath = 0f, tailSway = 0f, ahogeSway = 0f)
    }

    val glancing = 1f - device.arousal * 0.7f
    val glance = idle.look * profile.lookDeg * glancing

    // A glance turns the head *and* slides it a little. Rotation alone reads
    // weakly on rigid art, because the pivot sits well below the head.
    val glanceUnits = glance * 0.5f

    // The hand jostles her, so the tail picks up energy while it is moving.
    val tailEnergy = idle.tail * (2.2f + pose.happy * 1.8f) * profile.tailEnergy *
        (0.8f + device.energy * 1.4f)

    return KitsuMotion(
        blink = idle.blink,
        breath = idle.breath * profile.breathDepth,
        tailSway = tailEnergy,
        // The crest trails the weight shift instead of running its own clock.
        ahogeSway = idle.sway * 3.5f * profile.swayDepth,
        earTwitch = idle.earTwitch * (0.6f + profile.lookDeg / 7f * 0.5f),
        happy = pose.happy,
        tilt = pose.tilt,
        hop = pose.hop + device.bounce * 5f,
        // Amplified past the warp's resting factor so the shift reads as
        // shifting her weight rather than as a rounding error.
        sway = idle.sway * profile.swayDepth * 2.2f,
        lean = device.lean,
        headTurn = glance + device.headTurn,
        headShift = glanceUnits,
        headLift = device.headLift,
        earLag = device.earLag,
        shift = device.shift,
        squash = pose.squash + device.bounce * 0.5f,
        tailWhip = device.tailWhip
    )
}
