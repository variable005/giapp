package com.example.gitpulse

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder

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
            .build()
}
