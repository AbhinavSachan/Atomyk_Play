package com.atomykcoder.atomykplay.repository

import android.content.Context
import com.atomykcoder.atomykplay.interfaces.MusicDaoI
import com.atomykcoder.atomykplay.models.Music
import java.util.concurrent.CompletableFuture

class MusicRepository(context: Context) {
    private val musicDaoI: MusicDaoI
    private val context: Context

    init {
        musicDaoI = MusicDaoImpl()
        this.context = context
    }

    fun fetchMusicList(): CompletableFuture<ArrayList<Music>> {
        val future = CompletableFuture<ArrayList<Music>>()
        musicDaoI.fetchMusic(context)
            .thenAccept { value: ArrayList<Music> -> future.complete(value) }
            .exceptionally {
                future.completeExceptionally(it)
                null
            }
        return future
    }
}