package com.atomykcoder.atomykplay.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.activities.MainActivity
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.viewHolders.OpenPlayListViewHolder
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.enums.OptionSheetEnum
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage

class OpenPlayListAdapter(
    val context: Context,
    private var playlistName: String,
    items: ArrayList<Music?>?,
    onDragStartListener: OnDragStartListener
) : MusicAdapter(), ItemTouchHelperAdapter {
    val mainActivity: MainActivity
    var storage: StorageUtil
    var settingsStorage: SettingsStorage
    private var onDragStartListener: OnDragStartListener
    private val glideBuilt: GlideBuilt = GlideBuilt(context)


    init {
        super.items = items
        mainActivity = (context as MainActivity)
        this.onDragStartListener = onDragStartListener
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
                val item = super.items!![position]
                removeItem(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<Music> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.open_playlist_music_item, parent, false)
        return OpenPlayListViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenericViewHolder<Music>, position: Int) {
        super.onBindViewHolder(holder, position)
        val playListViewHolder = holder as OpenPlayListViewHolder
        val currentItem = super.items!![position]
        glideBuilt.loadAlbumArt(
            currentItem.path,
            R.drawable.ic_music,
            playListViewHolder.albumCoverIV,
            128,
            true
        )

        playListViewHolder.cardView.setOnClickListener {
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
        playListViewHolder.optBtn.setOnClickListener {
            if (isMusicNotAvailable(currentItem)) {
                return@setOnClickListener
            }
            mainActivity.openOptionMenu(currentItem, OptionSheetEnum.OPEN_PLAYLIST)
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

    fun removeItem(music: Music?) {
        val position = super.items!!.indexOf(music)
        if (position != -1) {
            super.items!!.removeAt(position)
            storage.removeItemInPlaylist(music, playlistName)
        }
        notifyItemRangeChanged(position, super.items!!.size - (position + 1))
        notifyItemRemoved(position)
    }
}