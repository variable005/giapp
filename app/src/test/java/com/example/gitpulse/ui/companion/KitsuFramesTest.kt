package com.example.gitpulse.ui.companion

import org.junit.Assert.assertTrue
import org.junit.Test

class KitsuFramesTest {

    @Test
    fun everySetCarriesFramesWithResources() {
        KitsuFrameSet.values().forEach { set ->
            val frames = KitsuFrameSets.getValue(set)
            assertTrue("$set has no frames", frames.isNotEmpty())
            frames.forEach { frame ->
                assertTrue("$set has a frame without a drawable", frame.res != 0)
            }
        }
    }

    @Test
    fun frameGeometryKeepsTheFoxPlanted() {
        KitsuFrameSets.values.flatten().forEach { frame ->
            assertTrue("bodyFrac out of range: ${frame.bodyFrac}", frame.bodyFrac > 0f && frame.bodyFrac <= 1f)
            assertTrue("anchorX out of range: ${frame.anchorX}", frame.anchorX >= 0f && frame.anchorX <= 1f)
            assertTrue("negative hold: ${frame.holdMs}", frame.holdMs >= 0)
        }
    }

    @Test
    fun onlyTheCuriousPoseIsHeld() {
        KitsuFrameSet.values().forEach { set ->
            val holds = KitsuFrameSets.getValue(set).any { it.holdMs == 0 }
            if (set == KitsuFrameSet.CURIOUS) {
                assertTrue("the curious tilt should hold", holds)
            } else {
                assertTrue("$set should advance on its own", !holds)
            }
        }
    }

    @Test
    fun idleIsTheOnlyLoopingSet() {
        assertTrue("the idle should loop", KitsuFrameSet.IDLE.loop)
        KitsuFrameSet.values().filter { it != KitsuFrameSet.IDLE }.forEach { set ->
            assertTrue("$set must hand back to the idle", !set.loop)
        }
    }

    @Test
    fun everyFrameOutlastsItsSetsCrossfade() {
        // The player waits holdMs - crossfadeMs before advancing, so a
        // crossfade longer than a hold would race the frames.
        KitsuFrameSet.values().forEach { set ->
            KitsuFrameSets.getValue(set).forEach { frame ->
                if (frame.holdMs > 0) {
                    assertTrue(
                        "$set frame holds ${frame.holdMs}ms but fades ${set.crossfadeMs}ms",
                        frame.holdMs > set.crossfadeMs
                    )
                }
            }
        }
    }

    @Test
    fun autoSpinDelaysStayInsideTheirMoodWindows() {
        val windows = mapOf(
            CompanionMood.THRIVING to (16_000L to 26_000L),
            CompanionMood.STEADY to (22_000L to 36_000L),
            CompanionMood.RESTING to (32_000L to 50_000L)
        )
        repeat(64) {
            windows.forEach { (mood, window) ->
                val delayMs = autoSpinDelayMs(mood)
                assertTrue(
                    "$mood spun outside its window: $delayMs",
                    delayMs >= window.first && delayMs < window.second
                )
            }
        }
    }
}
