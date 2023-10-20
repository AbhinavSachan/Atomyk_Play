package com.atomykcoder.atomykplay.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.viewHolders.MusicMainViewHolder
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.enums.OptionSheetEnum
import com.atomykcoder.atomykplay.repository.MusicRepo
import com.atomykcoder.atomykplay.ui.MainActivity
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage

class MusicMainAdapter(var context: Context, musicList: ArrayList<Music>?) : MusicAdapter() {
    private var mainActivity = context as MainActivity
    private var storage: StorageUtil = StorageUtil(context)
    private var settingsStorage: SettingsStorage = SettingsStorage(context)
    private var musicRepo: MusicRepo = MusicRepo.instance!!
    private val glideBuilt: GlideBuilt = GlideBuilt(context.applicationContext)

    init {
        super.items = musicList
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicListItems(newMusicArrayList: ArrayList<Music>?) {
        super.items?.clear()
        newMusicArrayList?.let { super.items?.addAll(it) }
        notifyDataSetChanged()
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
            glideBuilt.loadAlbumArt(
                currentItem.path,
                R.drawable.ic_music,
                musicViewHolder.albumCoverIV,
                128,
                true
            )
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

    override fun getItemId(position: Int): Long {
        val item = super.items?.get(position)
        return item?.id?.toLong() ?: 0
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
                musicRepo.removeFromList(item)
            }
            notifyItemRangeChanged(position, super.items!!.size - (position + 1))
            notifyItemRemoved(position)
            mainActivity.bottomSheetPlayerFragment?.removeMusicFromQueue(item)
        }

    }
}