package com.atomykcoder.atomykplay.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.activities.MainActivity
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.ViewHolders.MusicMainViewHolder
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.enums.OptionSheetEnum
import com.atomykcoder.atomykplay.helperFunctions.MusicDiffCallback
import com.atomykcoder.atomykplay.kotlin.ImageLoader
import com.atomykcoder.atomykplay.utils.MusicUtils
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage

class MusicMainAdapter(var context: Context, musicList: ArrayList<Music>?) : MusicAdapter() {
    var mainActivity: MainActivity
    var storage: StorageUtil
    var settingsStorage: SettingsStorage
    var musicUtils: MusicUtils
    var imageLoader: ImageLoader
    var glideBuilt: GlideBuilt

    init {
        mainActivity = context as MainActivity
        storage = StorageUtil(context)
        settingsStorage = SettingsStorage(context)
        musicUtils = MusicUtils.instance!!
        super.items = musicList
        imageLoader = ImageLoader(context)
        glideBuilt = GlideBuilt(context)
    }

    fun updateMusicListItems(newMusicArrayList: ArrayList<Music>?) {
        val diffCallback = MusicDiffCallback(super.items!!, newMusicArrayList!!)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        super.items?.clear()
        super.items?.addAll(newMusicArrayList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<Music> {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.music_item_layout, parent, false)
        return MusicMainViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenericViewHolder<Music>, position: Int) {
        super.onBindViewHolder(holder, position)
        val musicViewHolder = holder as MusicMainViewHolder
        val currentItem = super.items?.get(position)
        currentItem?.let {
            imageLoader.loadImage(R.drawable.ic_music, currentItem, musicViewHolder.albumCoverIV, 128)
            musicViewHolder.cardView.setOnClickListener {
                if (shouldIgnoreClick()) return@setOnClickListener
                if (isMusicNotAvailable(currentItem)) {
                    return@setOnClickListener
                }
                if (canPlay()) {
                    if (settingsStorage.loadKeepShuffle()) {
                        handleShuffle(mainActivity, currentItem, storage, position, super.items)
                    } else {
                        handleNoShuffle(mainActivity, currentItem, storage, position, super.items)
                    }
                }
            }
            musicViewHolder.optionButton.setOnClickListener {
                if (isMusicNotAvailable(currentItem)) {
                    return@setOnClickListener
                }
                mainActivity.openOptionMenu(currentItem, OptionSheetEnum.MAIN_LIST)
            }
        }
    }

    private fun isMusicNotAvailable(currentItem: Music): Boolean {
        if (!doesMusicExists(currentItem)) {
            Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show()
            removeItem(currentItem)
            return true
        }
        return false
    }

    fun removeItem(item: Music) {
        val position = super.items?.indexOf(item)
        position?.let {
            if (position != -1) {
                storage.removeFromInitialList(item)
                super.items!!.removeAt(position)
                musicUtils.removeFromList(item)
            }
            notifyItemRangeChanged(position, super.items!!.size - (position + 1))
            notifyItemRemoved(position)
            mainActivity.bottomSheetPlayerFragment?.queueAdapter?.removeItem(item)
        }

    }
}