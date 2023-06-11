package com.atomykcoder.atomykplay.customScripts

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.atomykcoder.atomykplay.ApplicationClass
import com.atomykcoder.atomykplay.fragments.TagEditorFragment.Companion.getRealPathFromImageURI
import com.atomykcoder.atomykplay.utils.MusicUtil.deleteAlbumArt
import com.atomykcoder.atomykplay.utils.MusicUtil.insertAlbumArt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.CannotWriteException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.tag.FieldDataInvalidException
import org.jaudiotagger.tag.TagException
import org.jaudiotagger.tag.datatype.Artwork
import java.io.File
import java.io.IOException

class TagWriter {

    companion object {

        private lateinit var mediaScannerConnection: MediaScannerConnection

        fun scan(context: Context, musicUri: Uri) {
            val file = File(musicUri.path.toString())
            val client = object : MediaScannerConnectionClient {
                override fun onScanCompleted(path: String?, uri: Uri?) {
                    mediaScannerConnection.disconnect()
                }

                override fun onMediaScannerConnected() {
                    mediaScannerConnection.scanFile(file.absolutePath, null)
                }
            }
            mediaScannerConnection = MediaScannerConnection(context, client)
            mediaScannerConnection.connect()
        }

        suspend fun writeTagsToFiles(context: Context, info: AudioTagInfo) {
            withContext(Dispatchers.IO) {
                var artwork: Artwork? = null
                var albumArtFile: File? = null
                if (info.artworkInfo?.artwork != null) {
                    try {
                        albumArtFile = File(
                            getRealPathFromImageURI(
                                context,
                                info.artworkInfo.artwork
                            )
                        )
                        artwork = Artwork.createArtworkFromFile(albumArtFile)
                    } catch (e: Exception) {
                        context.showToast("Something went wrong with artwork")
                    }
                }
                var wroteArtwork = false
                var deletedArtwork = false
                for (filePath in info.filePaths!!) {
                    try {
                        val audioFile = AudioFileIO.read(File(filePath))
                        val tag = audioFile.tagOrCreateAndSetDefault
                        if (info.fieldKeyValueMap != null) {
                            for ((key, value) in info.fieldKeyValueMap) {
                                try {
                                    tag.setField(key, value)
                                } catch (e: FieldDataInvalidException) {
                                    throw e
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        if (info.artworkInfo != null) {
                            if (info.artworkInfo.artwork == null) {
                                tag.deleteArtworkField()
                                deletedArtwork = true
                            } else if (artwork != null) {
                                tag.deleteArtworkField()
                                tag.setField(artwork)
                                wroteArtwork = true
                            }
                        }
                        audioFile.commit()
                    } catch (e: CannotReadException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: CannotWriteException) {
                        e.printStackTrace()
                    } catch (e: TagException) {
                        e.printStackTrace()
                    } catch (e: ReadOnlyFileException) {
                        e.printStackTrace()
                    } catch (e: InvalidAudioFrameException) {
                        e.printStackTrace()
                    }
                }
                if (wroteArtwork) {
                    insertAlbumArt(context, info.artworkInfo!!.albumId, albumArtFile!!.path)
                } else if (deletedArtwork) {
                    deleteAlbumArt(context, info.artworkInfo!!.albumId)
                }
                scan(context, info.filePaths[0].toUri())
            }
        }

        @RequiresApi(Build.VERSION_CODES.R)
        suspend fun writeTagsToFilesR(context: Context, info: AudioTagInfo): List<File> =
            withContext(Dispatchers.IO) {
                val cacheFiles = mutableListOf<File>()
                var artwork: Artwork? = null
                var albumArtFile: File? = null
                if (info.artworkInfo?.artwork != null) {
                    try {
                        albumArtFile = File(
                            getRealPathFromImageURI(
                                context,
                                info.artworkInfo.artwork
                            )
                        )
                        artwork = Artwork.createArtworkFromFile(albumArtFile)

                    } catch (e: IOException) {
                        context.showToast("Something went wrong with artwork")
                    }
                }
                var wroteArtwork = false
                var deletedArtwork = false
                for (filePath in info.filePaths!!) {
                    try {
                        val originFile = File(filePath)
                        val cacheFile = File(context.cacheDir, originFile.name)
                        cacheFiles.add(cacheFile)
                        originFile.inputStream().use { input ->
                            cacheFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        val audioFile = AudioFileIO.read(cacheFile)
                        val tag = audioFile.tagOrCreateAndSetDefault
                        if (info.fieldKeyValueMap != null) {
                            for ((key, value) in info.fieldKeyValueMap) {
                                try {
                                    tag.setField(key, value)
                                } catch (e: FieldDataInvalidException) {
                                    throw e
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        if (info.artworkInfo != null) {
                            if (info.artworkInfo.artwork == null) {
                                tag.deleteArtworkField()
                                deletedArtwork = true
                            } else if (artwork != null) {
                                tag.deleteArtworkField()
                                tag.setField(artwork)
                                wroteArtwork = true
                            }
                        }
                        audioFile.commit()
                    } catch (e: CannotReadException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: CannotWriteException) {
                        e.printStackTrace()
                    } catch (e: TagException) {
                        e.printStackTrace()
                    } catch (e: ReadOnlyFileException) {
                        e.printStackTrace()
                    } catch (e: InvalidAudioFrameException) {
                        e.printStackTrace()
                    }
                }
                if (wroteArtwork) {
                    insertAlbumArt(context, info.artworkInfo!!.albumId, albumArtFile!!.path)
                } else if (deletedArtwork) {
                    deleteAlbumArt(context, info.artworkInfo!!.albumId)
                }
                cacheFiles
            }
    }
}

private fun Context.showToast(s: String) {
    (this as ApplicationClass).showToast(s)
}
