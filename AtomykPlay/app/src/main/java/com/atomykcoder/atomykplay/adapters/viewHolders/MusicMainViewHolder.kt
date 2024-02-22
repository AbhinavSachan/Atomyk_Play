package com.atomykcoder.atomykplay.adapters.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration
import com.atomykcoder.atomykplay.models.Music

class MusicMainViewHolder(itemView: View) : GenericViewHolder<Music>(itemView) {
    val albumCoverIV: ImageView
    val optionButton: ImageView
    val cardView: RelativeLayout
    val nameText: TextView
    val artistText: TextView
    val durationText: TextView

    init {
        albumCoverIV = itemView.findViewById(R.id.song_album_cover)
        optionButton = itemView.findViewById(R.id.option)
        cardView = itemView.findViewById(R.id.cv_song_play)
        nameText = itemView.findViewById(R.id.song_name)
        artistText = itemView.findViewById(R.id.song_artist_name)
        durationText = itemView.findViewById(R.id.song_length)
    }

    override fun onBind(item: Music) {
        try {
            nameText.text = item.name
            artistText.text = item.artist
            durationText.text = convertDuration(item.duration)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
