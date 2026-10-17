package com.example.gitpulse.ui.companion

import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.example.gitpulse.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Flipbook animation for the companion art.
 *
 * The hand-keyed frames in drawable-nodpi (built by tools/build_kitsu_frames.py)
 * are played back by [KitsuFramePlayer]: an idle loop she lives in, plus
 * one-shot flourishes (the spin, the pounce) and a held pose for the curious
 * tilt. The player hands the scene a [KitsuFramePaint] — the pair of frames
 * to draw and how far the crossfade between them has run.
 *
 * Frame metadata carries everything the draw pass needs to keep her planted:
 * [KitsuFrame.bodyFrac] (what share of the image height the fox's body
 * occupies, so raised tails or baked leap padding never change her size) and
 * [KitsuFrame.anchorX] (the column her feet centroid sits on, so that column
 * can be centred on her ground line).
 */
internal enum class KitsuFrameSet(
    /** Loops forever (the idle) or plays once and hands back to the idle. */
    val loop: Boolean,
    /** Crossfade into each frame; 0 keeps the frames as hard cuts. */
    val crossfadeMs: Int
) {
    IDLE(loop = true, crossfadeMs = 260),
    SPIN(loop = false, crossfadeMs = 0),
    POUNCE(loop = false, crossfadeMs = 70),
    CURIOUS(loop = false, crossfadeMs = 140)
}

/** One flipbook frame and the geometry hints the scene needs to place it. */
internal data class KitsuFrame(
    @get:DrawableRes val res: Int,
    /** How long this frame stays up; 0 holds it until the next command. */
    val holdMs: Int,
    /** Fox body height as a share of the image height. */
    val bodyFrac: Float,
    /** Feet-centroid column as a share of the image width. */
    val anchorX: Float
)

/**
 * Frame tables. Regenerate with `python3 tools/build_kitsu_frames.py`; the
 * script prints this block with fresh anchor values.
 */
internal val KitsuFrameSets: Map<KitsuFrameSet, List<KitsuFrame>> = mapOf(
    KitsuFrameSet.IDLE to listOf(
        KitsuFrame(R.drawable.kitsu_idle_1, holdMs = 1700, bodyFrac = 0.996f, anchorX = 0.358f),
        KitsuFrame(R.drawable.kitsu_idle_2, holdMs = 1100, bodyFrac = 0.989f, anchorX = 0.328f),
    ),
    KitsuFrameSet.SPIN to listOf(
        KitsuFrame(R.drawable.kitsu_spin_1, holdMs = 95, bodyFrac = 0.996f, anchorX = 0.468f),
        KitsuFrame(R.drawable.kitsu_spin_2, holdMs = 95, bodyFrac = 0.996f, anchorX = 0.588f),
        KitsuFrame(R.drawable.kitsu_spin_3, holdMs = 95, bodyFrac = 0.996f, anchorX = 0.378f),
        KitsuFrame(R.drawable.kitsu_spin_4, holdMs = 95, bodyFrac = 0.996f, anchorX = 0.648f),
        KitsuFrame(R.drawable.kitsu_spin_5, holdMs = 95, bodyFrac = 0.990f, anchorX = 0.576f),
        KitsuFrame(R.drawable.kitsu_spin_6, holdMs = 95, bodyFrac = 0.996f, anchorX = 0.652f),
        KitsuFrame(R.drawable.kitsu_spin_7, holdMs = 95, bodyFrac = 0.996f, anchorX = 0.677f),
        KitsuFrame(R.drawable.kitsu_spin_8, holdMs = 95, bodyFrac = 0.996f, anchorX = 0.452f),
    ),
    KitsuFrameSet.POUNCE to listOf(
        KitsuFrame(R.drawable.kitsu_pounce_1, holdMs = 150, bodyFrac = 0.854f, anchorX = 0.255f),
        KitsuFrame(R.drawable.kitsu_pounce_2, holdMs = 130, bodyFrac = 0.823f, anchorX = 0.244f),
        KitsuFrame(R.drawable.kitsu_pounce_3, holdMs = 320, bodyFrac = 0.985f, anchorX = 0.283f),
    ),
    KitsuFrameSet.CURIOUS to listOf(
        KitsuFrame(R.drawable.kitsu_curious_1, holdMs = 0, bodyFrac = 0.996f, anchorX = 0.442f),
    )
)

/** A decoded frame ready to draw. */
internal data class KitsuFrameArt(
    val frame: KitsuFrame,
    val image: ImageBitmap
)

/**
 * What the scene should paint this frame: [current] crossfades in over
 * [previous] using the player's blend value, which is read inside the draw
 * pass so the fade never recomposes the screen.
 */
internal data class KitsuFramePaint(
    val current: KitsuFrameArt,
    val previous: KitsuFrameArt? = null
)

/**
 * Decodes the frame drawables once and keeps them for the life of the hero.
 * The whole set is a few megabytes of small WebPs; caching keeps every
 * reaction instant instead of decoding on the tap.
 */
internal class KitsuFrameLoader(context: Context) {

    private val resources = context.applicationContext.resources
    private val cache = mutableMapOf<Int, ImageBitmap>()

    @Synchronized
    fun art(frame: KitsuFrame): KitsuFrameArt? {
        cache[frame.res]?.let { return KitsuFrameArt(frame, it) }
        return runCatching {
            val bitmap = BitmapFactory.decodeResource(resources, frame.res)
                ?: return null
            bitmap.asImageBitmap().also { cache[frame.res] = it }
        }.getOrNull()?.let { KitsuFrameArt(frame, it) }
    }
}

/**
 * Drives the flipbook. Commands arrive on a conflated channel (the newest
 * request wins, taps never queue up); the loop below owns all timing.
 */
internal class KitsuFramePlayer(
    private val loader: KitsuFrameLoader,
    private val reducedMotion: Boolean
) {
    /** The pair of frames to draw; null only while the first decode runs. */
    var paint by mutableStateOf<KitsuFramePaint?>(null)
        private set

    /** Crossfade position 0..1 for the current [paint]; read in the draw pass. */
    val blend = Animatable(1f)

    private sealed interface Command {
        data class Once(val set: KitsuFrameSet) : Command
        data class Hold(val set: KitsuFrameSet) : Command
        data object Resume : Command
    }

    private val commands = Channel<Command>(Channel.CONFLATED)

    init {
        // Decode the opening pose up front: the fox is on screen from the
        // very first drawn frame, with no placeholder flash.
        loader.art(KitsuFrameSets.getValue(KitsuFrameSet.IDLE).first())?.let { art ->
            paint = KitsuFramePaint(art)
        }
    }

    /** Play [set] once, then settle back into the idle loop. */
    fun playOnce(set: KitsuFrameSet) {
        if (!reducedMotion) commands.trySend(Command.Once(set))
    }

    /** Show [set]'s held pose until [resume] is called. */
    fun hold(set: KitsuFrameSet) {
        if (!reducedMotion) commands.trySend(Command.Hold(set))
    }

    /** Leave a held pose and return to the idle loop. */
    fun resume() {
        if (!reducedMotion) commands.trySend(Command.Resume)
    }

    /** Decode the frames off the main thread, so reactions never wait on disk. */
    suspend fun warmUp() {
        if (reducedMotion) return
        withContext(Dispatchers.Default) {
            KitsuFrameSets.values.forEach { frames -> frames.forEach { loader.art(it) } }
        }
    }

    suspend fun run(scope: CoroutineScope) {
        if (reducedMotion) {
            // Park on the calm pose; commands are ignored.
            loader.art(KitsuFrameSets.getValue(KitsuFrameSet.IDLE).first())?.let { art ->
                paint = KitsuFramePaint(art)
                blend.snapTo(1f)
            }
            return
        }

        var active = KitsuFrameSet.IDLE
        var held: KitsuFrameSet? = null
        var index = 0
        var instant = true

        while (true) {
            val frames = KitsuFrameSets.getValue(active)
            val frame = frames[index % frames.size]
            val art = loader.art(frame)

            if (art == null) {
                // Decode failed or is still settling; retry without losing place.
                delay(32)
                continue
            }

            show(scope, art, if (instant) 0 else active.crossfadeMs)
            instant = false

            if (frame.holdMs <= 0) {
                // A held frame: only a command moves her off it.
                active = apply(commands.receive()) { held = it }
                index = 0
                continue
            }

            val wait = (frame.holdMs - active.crossfadeMs).coerceAtLeast(40).toLong()
            val command = withTimeoutOrNull(wait) { commands.receive() }
            if (command != null) {
                active = apply(command) { held = it }
                index = 0
                continue
            }

            index += 1
            if (index >= frames.size) {
                index = 0
                if (!active.loop) active = held ?: KitsuFrameSet.IDLE
            }
        }
    }

    private fun apply(
        command: Command,
        setHeld: (KitsuFrameSet?) -> Unit
    ): KitsuFrameSet = when (command) {
        is Command.Once -> {
            setHeld(null)
            command.set
        }
        is Command.Hold -> {
            setHeld(command.set)
            command.set
        }
        Command.Resume -> {
            setHeld(null)
            KitsuFrameSet.IDLE
        }
    }

    /**
     * Puts [art] on screen. With a crossfade the fade runs in the background
     * so the command loop stays responsive; a newer [show] cancels it by
     * snapping [blend] back to zero.
     */
    private suspend fun show(scope: CoroutineScope, art: KitsuFrameArt, crossMs: Int) {
        val previous = paint?.current
        if (previous == null || previous.frame.res == art.frame.res || crossMs <= 0) {
            paint = KitsuFramePaint(art)
            blend.snapTo(1f)
            return
        }
        paint = KitsuFramePaint(art, previous)
        blend.snapTo(0f)
        scope.launch {
            blend.animateTo(1f, tween(crossMs, easing = LinearEasing))
            if (paint?.current?.frame?.res == art.frame.res) paint = KitsuFramePaint(art)
        }
    }
}

/**
 * Creates the flipbook player for the hero. The idle frames decode inline so
 * the fox is on screen from the first frame; everything else warms up in the
 * background, long before the first tap needs it.
 */
@Composable
internal fun rememberKitsuFramePlayer(reducedMotion: Boolean): KitsuFramePlayer {
    val context = LocalContext.current
    val player = remember(reducedMotion) {
        KitsuFramePlayer(KitsuFrameLoader(context), reducedMotion)
    }

    LaunchedEffect(player) { player.warmUp() }

    val scope = rememberCoroutineScope()
    LaunchedEffect(player, scope) { player.run(scope) }

    return player
}
