package com.atomykcoder.atomykplay.classes

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.atomykcoder.atomykplay.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

class GlideBuilt(private val context: Context) {
    fun glide(uri: String?, placeholderImage: Int, imageView: ImageView?, image_measure: Int) {
        try {
            Glide.with(context).load(uri).apply(
                RequestOptions().placeholder(placeholderImage).error(R.drawable.ic_music_thumbnail)
            )
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(true)
                .override(image_measure, image_measure)
                .into(imageView!!)
        } catch (ignored: Exception) {
        }
    }

    fun glideBitmap(
        newBitmap: Bitmap?,
        placeholderImage: Int,
        imageView: ImageView?,
        image_measure: Int,
        animate: Boolean
    ) {
        var shadowed = placeholderImage
        if (newBitmap != null) {
            shadowed = -1
        }
        val drawableTransitionOptions: DrawableTransitionOptions = if (animate) {
            DrawableTransitionOptions.withCrossFade(300)
        } else {
            DrawableTransitionOptions().dontTransition()
        }
        try {
            Glide.with(context).load(newBitmap).apply(
                RequestOptions().placeholder(shadowed).error(R.drawable.ic_music_thumbnail)
            )
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .transition(drawableTransitionOptions)
                .override(image_measure, image_measure)
                .into(imageView!!)
        } catch (ignored: Exception) {
        }
    }
}