package com.atomykcoder.atomykplay.adapters.viewHolders

import android.view.View
import android.widget.TextView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder

class FoundLyricsViewHolder(itemView: View) : GenericViewHolder<String>(itemView) {
    var song_title: TextView
    var song_sampleLyrics: TextView

    init {
        song_title = itemView.findViewById(R.id.found_song_title)
        song_sampleLyrics = itemView.findViewById(R.id.found_song_lyrics_sample)
    }
}
