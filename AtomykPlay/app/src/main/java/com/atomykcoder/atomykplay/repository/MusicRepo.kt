package com.atomykcoder.atomykplay.repository

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.atomykcoder.atomykplay.data.Music
import java.util.concurrent.CompletableFuture

class MusicRepo {
    private val status = MutableLiveData<LoadingStatus>()
    var initialMusicList = ArrayList<Music>()
    fun fetchMusic(activity: Activity): CompletableFuture<Void?> {
        val future = CompletableFuture<Void?>()
        val repository = MusicRepository(activity.applicationContext)
        activity.runOnUiThread { status.setValue(LoadingStatus.LOADING) }
        repository.fetchMusicList().thenAccept {
            initialMusicList = ArrayList(it)
            activity.runOnUiThread { status.setValue(LoadingStatus.SUCCESS) }
            future.complete(null)
        }.exceptionally {
            activity.runOnUiThread { status.setValue(LoadingStatus.FAILURE) }
            future.completeExceptionally(it)
            null
        }
        return future

    }

    fun removeFromList(music: Music) {
        initialMusicList.remove(music)
    }

    fun getStatus(): LiveData<LoadingStatus> {
        return status
    }

    companion object {
        @JvmStatic
        var instance: MusicRepo? = null
            get() {
                if (field == null) {
                    field = MusicRepo()
                }
                return field
            }
    }
}