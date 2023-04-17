package com.atomykcoder.atomykplay.utils

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.repository.LoadingStatus
import com.atomykcoder.atomykplay.repository.MusicRepository
import java.util.concurrent.CompletableFuture

class MusicUtils {
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
        var instance: MusicUtils? = null
            get() {
                if (field == null) {
                    field = MusicUtils()
                }
                return field
            }
    }
}