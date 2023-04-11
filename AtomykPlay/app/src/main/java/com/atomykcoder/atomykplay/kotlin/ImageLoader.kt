package com.atomykcoder.atomykplay.kotlin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.widget.ImageView
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.data.Music
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class ImageLoader(context: Context) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineScopeMain: CoroutineScope = CoroutineScope(Dispatchers.Main)
    private val glideBuilt: GlideBuilt = GlideBuilt(context)

    fun loadImage(placeholder: Int, music: Music, imageView: ImageView, measure: Int) {
        var result: Bitmap?
        coroutineScope.launch {
            result = loadFromMedia(music)
            coroutineScopeMain.launch {
                glideBuilt.glideBitmap(result, placeholder, imageView, measure, false)
            }
        }
    }
    private fun loadFromMedia(item: Music): Bitmap? {
        var image: Bitmap? = null
        var art: ByteArray? = null // initialize to null

        if (File(item.path).exists()) {
            try {
                MediaMetadataRetriever().use { mediaMetadataRetriever ->
                    mediaMetadataRetriever.setDataSource(item.path)
                    art = mediaMetadataRetriever.embeddedPicture
                }
            } catch (ignored: IOException) {
            }

            if (art != null) { // add null check
                try {
                    image = BitmapFactory.decodeByteArray(art, 0, art!!.size)
                } catch (ignored: Exception) {
                }
            }
        }
        return image
    }
}