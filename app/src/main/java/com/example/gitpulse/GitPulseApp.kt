package com.example.gitpulse

import android.app.Application
import android.content.pm.ApplicationInfo
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.util.DebugLogger

/**
 * App-wide Coil loader.
 *
 * README badges are almost always SVGs (shields.io and friends), so the
 * default loader gains an SVG decoder alongside the regular bitmap decoders.
 */
class GitPulseApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .crossfade(true)
            .apply {
                // Surface fetch/decode failures in logcat on debuggable builds.
                val debuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                if (debuggable) logger(DebugLogger())
            }
            .build()
}
