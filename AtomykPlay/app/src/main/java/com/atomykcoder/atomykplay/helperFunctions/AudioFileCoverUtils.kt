package com.atomykcoder.atomykplay.helperFunctions

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.TagException
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

object AudioFileCoverUtils {
    private val FALLBACKS = arrayOf(
        "cover.jpg", "album.jpg", "folder.jpg",
        "cover.png", "album.png", "folder.png",
        "cover.webp", "album.webp", "folder.webp"
    )

    @Throws(FileNotFoundException::class)
    fun fallback(path: String?): InputStream? {
        // Method 1: use embedded high resolution album art if there is any
        try {
            val mp3File = MP3File(path)
            if (mp3File.hasID3v2Tag()) {
                val art = mp3File.tag.firstArtwork
                if (art != null) {
                    val imageData = art.binaryData
                    return ByteArrayInputStream(imageData)
                }
            }
            // If there are any exceptions, we ignore them and continue to the other fallback method
        } catch (ignored: ReadOnlyFileException) {
        } catch (ignored: InvalidAudioFrameException) {
        } catch (ignored: TagException) {
        } catch (ignored: IOException) {
        }

        // Method 2: look for album art in external files
        val parent = path?.let { File(it).parentFile }
        for (fallback in FALLBACKS) {
            val cover = File(parent, fallback)
            if (cover.exists()) {
                return FileInputStream(cover)
            }
        }
        return null
    }
}