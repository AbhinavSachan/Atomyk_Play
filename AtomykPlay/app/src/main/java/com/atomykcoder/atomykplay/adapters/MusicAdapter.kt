package com.atomykcoder.atomykplay.adapters

import android.os.Handler
import android.os.Looper
import com.atomykcoder.atomykplay.adapters.generics.GenericRecyclerAdapter
import com.atomykcoder.atomykplay.constants.ShuffleModes
import com.atomykcoder.atomykplay.models.Music
import com.atomykcoder.atomykplay.ui.MainActivity
import com.atomykcoder.atomykplay.utils.StorageUtil
import java.io.File
import java.util.concurrent.Executors

open class MusicAdapter : GenericRecyclerAdapter<Music>() {
    val handler = Handler(Looper.getMainLooper())
    var canPlay = true
    private var musicList: ArrayList<Music> = arrayListOf()
    private fun handlePlayMusic(mainActivity: MainActivity, item: Music?) {
        mainActivity.playAudio(item)
        if (mainActivity.bottomSheetPlayerFragment != null) {
            mainActivity.bottomSheetPlayerFragment!!.updateQueueAdapter(musicList)
        }
        mainActivity.openBottomPlayer()
    }

    fun canPlay(): Boolean {
        return canPlay
    }

    protected fun handleShuffle(
        mainActivity: MainActivity,
        item: Music?,
        storage: StorageUtil,
        position: Int,
        musicList: ArrayList<Music>?
    ) {
        if (musicList == null) return
        val service = Executors.newFixedThreadPool(3)
        val shuffleList = ArrayList(musicList)
        canPlay = false
        service.execute {
            storage.saveShuffle(ShuffleModes.SHUFFLE_MODE_ALL)
            storage.saveTempMusicList(shuffleList)
            shuffleList.remove(item)

            //shuffling list
            shuffleList.shuffle()

            //adding the removed item in shuffled list on 0th index
            shuffleList.add(0, item)

            //saving list
            storage.saveQueueList(shuffleList)
            storage.saveMusicIndex(0)
            this.musicList = shuffleList
            handler.post {
                handlePlayMusic(mainActivity, item)
                canPlay = true
            }
        }
        service.shutdown()
    }

    protected fun handleNoShuffle(
        mainActivity: MainActivity,
        item: Music?,
        storage: StorageUtil,
        position: Int,
        musicList: ArrayList<Music>?
    ) {
        if (musicList == null) return
        val service = Executors.newFixedThreadPool(2)
        canPlay = false
        service.execute {
            storage.saveShuffle(ShuffleModes.SHUFFLE_MODE_NONE)
            storage.saveQueueList(musicList)
            storage.saveMusicIndex(position)
            this.musicList = musicList
            handler.post {
                handlePlayMusic(mainActivity, item)
                canPlay = true
            }
        }
        service.shutdown()
    }

    protected fun doesMusicExists(music: Music): Boolean {
        val file = File(music.path)
        return file.exists()
    }
}
