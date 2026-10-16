package com.example.gitpulse.ui.companion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Color
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Transform
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberFillLightNode
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * Kitsu, rendered in three dimensions by Filament (SceneView).
 *
 * The view is transparent — `isOpaque = false` on a texture surface — so the
 * 2D meadow and sky keep drawing behind it on the same Canvas; only the fox
 * itself is 3D now. The model comes from the Blender pipeline
 * (`assets/kitsu.glb`), posed by name: the tails (`tail_0..2`) sway about
 * their bases, the scarf flaps (`flap_1` / `flap_-1`) swing, the eye trio
 * (`eye_base` / `iris` / `hl` per side) blinks by squashing about its
 * centers, and the star orb floats and pulses. Parts missing from the asset
 * simply stay still — the whole-body pose always works.
 *
 * The render loop runs while the fox is on screen — the idle drift writes node
 * transforms on every composition frame, and each mutator marks the scene dirty.
 * Under reduced motion the fox is posed once and the loop parks after a short
 * dirty window, so a still hero stops drawing entirely.
 *
 * [onReady] fires once the model node exists; the caller keeps the 2D fox
 * visible until then, so the hero is never empty. If the model never loads,
 * the 2D fox simply stays.
 */
@Composable
fun Kitsu3DFox(
    motion: KitsuMotion,
    night: Boolean,
    reducedMotion: Boolean,
    onReady: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val instance = rememberModelInstance(modelLoader, "kitsu.glb")

    // Lights pick the fox's mood from the theme: warm daylight or cool
    // moonlit night. The moon keeps more fill so the night fox stays readable.
    val mainLight = rememberMainLightNode(engine)
    val fillLight = rememberFillLightNode(engine)
    LaunchedEffect(night, mainLight, fillLight) {
        if (night) {
            mainLight?.color = Color(0.60f, 0.70f, 1.00f)
            mainLight?.intensity = 24_000f
            fillLight?.color = Color(0.45f, 0.55f, 0.90f)
            fillLight?.intensity = 7_000f
        } else {
            mainLight?.color = Color(1.00f, 0.95f, 0.86f)
            mainLight?.intensity = 85_000f
            fillLight?.color = Color(0.92f, 0.95f, 1.00f)
            fillLight?.intensity = 16_000f
        }
    }

    var rig by remember { mutableStateOf<FoxRig?>(null) }
    val latestMotion by rememberUpdatedState(motion)

    // Reduced motion: after the one calm pose has had time to present, park the
    // render loop so an idle hero costs no frames. The loop settles rather than
    // spins while parked, and the async model keeps it running until finalised.
    var posed by remember { mutableStateOf(false) }
    var settled by remember { mutableStateOf(false) }
    LaunchedEffect(posed) {
        if (posed) {
            delay(400)
            settled = true
        }
    }

    SceneView(
        modifier = modifier,
        engine = engine,
        modelLoader = modelLoader,
        surfaceType = SurfaceType.TextureSurface,
        isOpaque = false,
        isRendering = !reducedMotion || !settled || modelLoader.progress < 1f,
        // The fox is placed in authored world space: standing on y = 0 with
        // its feet on the 2D contact shadow, ~52% of the box tall, framed
        // from the same front-upper-left 3/4 angle as the look-dev renders.
        autoCenterContent = false,
        cameraManipulator = null,
        cameraNode = rememberCameraNode(engine) {
            position = Position(1.27f, 0.65f, -0.50f)
            lookAt(Position(0f, 0.30f, 0f))
        },
        mainLightNode = mainLight,
        fillLightNode = fillLight
    ) {
        instance?.let { model ->
            ModelNode(
                modelInstance = model,
                autoAnimate = false,
                apply = { rig = FoxRig(this) }
            )
        }
    }

    // First pose as soon as the node exists, then hand over from the 2D fox.
    LaunchedEffect(rig) {
        val node = rig ?: return@LaunchedEffect
        node.pose(latestMotion)
        onReady()
        posed = true
    }

    // Idle driver: one transform write per frame the hero recomposes. Runs on
    // the composition frame clock, which the hero's animations already keep
    // ticking. Reduced motion poses once (above) and never writes again.
    if (!reducedMotion) {
        SideEffect { rig?.pose(motion) }
    }
}

/**
 * Named handles onto the parts of the exported fox the app animates, with
 * each part's authored transform captured as the base to pose against.
 */
private class FoxRig(modelNode: ModelNode) {

    /** A movable part: its authored pose plus a writer for the live node. */
    private class Part(
        val pos: Position,
        val rot: Rotation,
        val scale: Scale,
        val set: (Transform) -> Unit
    )

    private val model = modelNode
    private val base = modelNode.transform

    private fun part(name: String): Part? =
        model.renderableNodes.firstOrNull { it.name == name }?.let { node ->
            Part(node.position, node.rotation, node.scale) { node.transform = it }
        }

    private val tails = listOfNotNull(
        part("tail_0"),
        part("tail_1"),
        part("tail_2")
    )

    private val flaps = listOfNotNull(
        part("flap_1"),
        part("flap_-1")
    )

    private val eyes = listOfNotNull(
        part("eye_base"),
        part("eye_base.001"),
        part("iris"),
        part("iris.001"),
        part("hl"),
        part("hl.001")
    )

    private val orb = part("orb")

    fun pose(motion: KitsuMotion) {
        val t = System.nanoTime() / 1_000_000_000f

        // Whole body: breathing bob, tap-hop lift and the happy rise, plus the
        // curious lean on the long-press.
        model.transform = Transform(
            position = Position(
                base.position.x,
                base.position.y +
                    motion.breath * 0.006f +
                    motion.hop * 0.0043f +
                    motion.happy * 0.006f,
                base.position.z
            ),
            rotation = Rotation(x = motion.tilt * 1.35f),
            scale = base.scale
        )

        // Tails: the shared sway phase, per-tail staggered, on top of a slow
        // drift so the plume never sits dead still.
        tails.forEachIndexed { i, part ->
            val yaw = motion.tailSway * (0.75f + 0.28f * i) + sin(t * 1.15f + i * 2.1f) * 1.1f
            val roll = sin(t * 0.85f + i * 1.7f) * 0.9f - motion.breath * 0.6f
            part.set(Transform(
                position = part.pos,
                rotation = Rotation(
                    x = part.rot.x,
                    y = part.rot.y + yaw,
                    z = part.rot.z + roll
                ),
                scale = part.scale
            ))
        }

        // Scarf flaps: a gentle pendulum swing, couple to the tail phase.
        flaps.forEachIndexed { i, part ->
            val swing = sin(t * 1.45f + i * 1.9f) * 2.6f + motion.tailSway * 0.22f
            part.set(Transform(
                position = part.pos,
                rotation = Rotation(
                    x = part.rot.x + swing,
                    y = part.rot.y,
                    z = part.rot.z + swing * 0.4f
                ),
                scale = part.scale
            ))
        }

        // Eyes: the shared blink squashes them vertically about their centers.
        val blink = motion.blink.coerceIn(0.06f, 1f)
        eyes.forEach { part ->
            part.set(Transform(
                position = part.pos,
                rotation = part.rot,
                scale = Scale(
                    x = part.scale.x,
                    y = part.scale.y * blink,
                    z = part.scale.z
                )
            ))
        }

        // Star orb: a slow float, a breath pulse, and a flare whenever the
        // fox is happy (tap or fresh sync).
        orb?.let { part ->
            val float = sin(t * 0.9f) * 0.008f
            val pulse = 1f + 0.05f * sin(t * 2.2f) + motion.happy * 0.16f
            part.set(Transform(
                position = Position(
                    part.pos.x,
                    part.pos.y + float + motion.happy * 0.015f,
                    part.pos.z
                ),
                rotation = part.rot,
                scale = Scale(
                    x = part.scale.x * pulse,
                    y = part.scale.y * pulse,
                    z = part.scale.z * pulse
                )
            ))
        }
    }
}
