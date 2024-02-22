package com.atomykcoder.atomykplay.adapters.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration
import com.atomykcoder.atomykplay.models.Music

class MusicQueueViewHolder(itemView: View) : GenericViewHolder<Music>(itemView) {
    val albumCoverIV: ImageView
    val dragButton: ImageView
    val cardView: View
    val nameText: TextView
    val artistText: TextView
    val durationText: TextView
    val musicIndex: TextView

    init {
        albumCoverIV = itemView.findViewById(R.id.song_album_cover_queue)
        dragButton = itemView.findViewById(R.id.drag_i_btn_queue)
        cardView = itemView.findViewById(R.id.cv_song_play_queue)
        nameText = itemView.findViewById(R.id.song_name_queue)
        musicIndex = itemView.findViewById(R.id.song_index_num_queue)
        artistText = itemView.findViewById(R.id.song_artist_name_queue)
        durationText = itemView.findViewById(R.id.song_length_queue)
    }

    override fun onBind(item: Music) {
        nameText.text = item.name
        artistText.text = item.artist
        durationText.text = convertDuration(item.duration)
    }
}
