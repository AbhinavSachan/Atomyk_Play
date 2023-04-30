package com.atomykcoder.atomykplay.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.activities.MainActivity
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.viewHolders.MusicMainViewHolder
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.enums.OptionSheetEnum
import com.atomykcoder.atomykplay.helperFunctions.MusicDiffCallback
import com.atomykcoder.atomykplay.helperFunctions.ImageLoader
import com.atomykcoder.atomykplay.repository.MusicRepo
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicMainAdapter(var context: Context, musicList: ArrayList<Music>?) : MusicAdapter() {
    private var mainActivity: MainActivity = context as MainActivity
    private var storage: StorageUtil = StorageUtil(context)
    private var settingsStorage: SettingsStorage = SettingsStorage(context)
    private var musicRepo: MusicRepo = MusicRepo.instance!!
    private var imageLoader: ImageLoader
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineScopeMain: CoroutineScope = CoroutineScope(Dispatchers.Main)
    private val glideBuilt: GlideBuilt = GlideBuilt(context)

    init {
        super.items = musicList
        imageLoader = ImageLoader(context)
    }

    fun updateMusicListItems(newMusicArrayList: ArrayList<Music>?) {
        val diffCallback = MusicDiffCallback(super.items!!, newMusicArrayList!!)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        super.items?.clear()
        super.items?.addAll(newMusicArrayList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<Music> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.music_item_layout, parent, false)
        return MusicMainViewHolder(view)
    }
    override fun onBindViewHolder(holder: GenericViewHolder<Music>, position: Int) {
        super.onBindViewHolder(holder, position)
        val musicViewHolder = holder as MusicMainViewHolder
        val currentItem = super.items?.get(position)
        currentItem?.let {
            var result:Bitmap?
            coroutineScope.launch {
                result = imageLoader.loadImage(currentItem)
                coroutineScopeMain.launch{
                    glideBuilt.glideBitmap(result, R.drawable.ic_music, musicViewHolder.albumCoverIV, 128, false)
                }
            }
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
                musicRepo.removeFromList(item)
            }
            notifyItemRangeChanged(position, super.items!!.size - (position + 1))
            notifyItemRemoved(position)
            mainActivity.bottomSheetPlayerFragment?.queueAdapter?.removeItem(item)
        }

    }
}