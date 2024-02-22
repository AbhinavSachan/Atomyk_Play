package com.atomykcoder.atomykplay.adapters.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.models.Playlist

class PlaylistViewHolder(view: View) : GenericViewHolder<Playlist>(view) {
    var playlistName: TextView
    var songCount: TextView
    var coverIV: ImageView
    var optImg: ImageView
    var cardView: View

    init {
        playlistName = view.findViewById(R.id.playlist_name_tv)
        songCount = view.findViewById(R.id.playlist_item_count_tv)
        coverIV = view.findViewById(R.id.playlist_cover_img)
        optImg = view.findViewById(R.id.playlist_option)
        cardView = view.findViewById(R.id.playlist_card)
    }
}
