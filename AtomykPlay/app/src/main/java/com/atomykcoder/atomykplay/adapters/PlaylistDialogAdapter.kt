package com.atomykcoder.atomykplay.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericRecyclerAdapter
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.viewHolders.PlayListDialogViewHolder
import com.atomykcoder.atomykplay.models.Music
import com.atomykcoder.atomykplay.models.Playlist
import com.atomykcoder.atomykplay.ui.MainActivity
import com.atomykcoder.atomykplay.utils.StorageUtil

class PlaylistDialogAdapter(
    private val context: Context,
    _playlists: ArrayList<Playlist>?,
    _music: Music?
) : GenericRecyclerAdapter<Playlist>() {
    private val music: Music?

    init {
        super.items = _playlists
        music = _music
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<Playlist> {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.playlist_dialog_item_layout, parent, false)
        return PlayListDialogViewHolder(view)
    }

    override fun onBindViewHolder(_holder: GenericViewHolder<Playlist>, position: Int) {
        val holder = _holder as PlayListDialogViewHolder
        assert(super.items != null)
        val playlist = super.items?.get(position)
        val mainActivity = context as? MainActivity?
        val storageUtil = StorageUtil(context)
        holder.textView.text = playlist?.name
        holder.view.setOnClickListener { v: View? ->
            storageUtil.saveItemInPlayList(music, playlist?.name)
            mainActivity?.dismissPlDialog()
            Toast.makeText(context, "added to " + playlist?.name, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return super.items!!.size
    }
}
