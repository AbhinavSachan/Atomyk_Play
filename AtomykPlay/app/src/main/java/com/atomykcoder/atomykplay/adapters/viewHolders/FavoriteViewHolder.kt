package com.atomykcoder.atomykplay.adapters.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration
import com.atomykcoder.atomykplay.models.Music

class FavoriteViewHolder(itemView: View) : GenericViewHolder<Music>(itemView) {
    val albumCoverIV: ImageView
    val optBtn: ImageView
    val cardView: View
    val nameText: TextView
    val artistText: TextView
    val durationText: TextView

    init {
        albumCoverIV = itemView.findViewById(R.id.song_album_cover_favorite)
        optBtn = itemView.findViewById(R.id.favorite_itemOpt)
        cardView = itemView.findViewById(R.id.cv_song_play_favorite)
        nameText = itemView.findViewById(R.id.song_name_favorite)
        artistText = itemView.findViewById(R.id.song_artist_name_favorite)
        durationText = itemView.findViewById(R.id.song_length_favorite)
    }

    override fun onBind(item: Music) {
        nameText.text = item.name
        artistText.text = item.artist
        durationText.text = convertDuration(item.duration)
    }
}
