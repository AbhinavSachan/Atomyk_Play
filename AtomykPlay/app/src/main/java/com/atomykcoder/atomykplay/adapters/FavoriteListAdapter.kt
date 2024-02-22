package com.atomykcoder.atomykplay.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.viewHolders.FavoriteViewHolder
import com.atomykcoder.atomykplay.enums.OptionSheetEnum
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener
import com.atomykcoder.atomykplay.models.Music
import com.atomykcoder.atomykplay.ui.MainActivity
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage
import com.atomykcoder.atomykplay.utils.loadAlbumArt

class FavoriteListAdapter(
    private val context: Context,
    _musicList: ArrayList<Music>?,
    onDragStartListener: OnDragStartListener
) : MusicAdapter(), ItemTouchHelperAdapter {
    var mainActivity: MainActivity
    private var onDragStartListener: OnDragStartListener
    var storage: StorageUtil
    var settingsStorage: SettingsStorage

    init {
        super.items = _musicList
        this.onDragStartListener = onDragStartListener
        mainActivity = (context as MainActivity)
        storage = StorageUtil(context)
        settingsStorage = SettingsStorage(context)
    }

    //when item starts to move it will change positions of every item in real time
    override fun onItemMove(fromPos: Int, toPos: Int) {}

    //removing item on swipe
    override fun onItemDismiss(position: Int) {
        //if any item has been removed this will save new list on temp list
        if (super.items != null) {
            if (position != -1 && position < super.items!!.size) {
                removeItem(super.items!![position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<Music> {
        val view =
            LayoutInflater.from(context).inflate(R.layout.favorite_item_layout, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(_holder: GenericViewHolder<Music>, position: Int) {
        super.onBindViewHolder(_holder, position)
        val holder = _holder as FavoriteViewHolder
        val currentItem = super.items!![position]
        holder.albumCoverIV.loadAlbumArt(
            currentItem.path,
            R.drawable.ic_music,
            128,
            true
        )
        holder.cardView.setOnClickListener {
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
        holder.optBtn.setOnClickListener {
            if (isMusicNotAvailable(currentItem)) {
                return@setOnClickListener
            }
            mainActivity.openOptionMenu(currentItem, OptionSheetEnum.FAVORITE_LIST)
        }
    }

    override fun getItemCount(): Int {
        return super.items!!.size
    }

    private fun isMusicNotAvailable(currentItem: Music): Boolean {
        if (!doesMusicExists(currentItem)) {
            Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show()
            removeItem(currentItem)
            return true
        }
        return false
    }

    fun removeItem(item: Music?) {
        val position = super.items!!.indexOf(item)
        if (position != -1) {
            super.items!!.removeAt(position)
            storage.removeFavorite(item)
        }
        notifyItemRangeChanged(position, super.items!!.size - (position + 1))
        notifyItemRemoved(position)
    }
}