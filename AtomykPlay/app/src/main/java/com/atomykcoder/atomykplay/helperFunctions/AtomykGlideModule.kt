package com.atomykcoder.atomykplay.helperFunctions

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream

@GlideModule
class AtomykMusicGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            AudioFileCover::class.java,
            InputStream::class.java,
            AudioFileCoverLoader.Factory()
        )
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}