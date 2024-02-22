package com.atomykcoder.atomykplay.adapters.viewHolders

import android.view.View
import android.widget.TextView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder

class MusicLyricsViewHolder(itemView: View) : GenericViewHolder<String>(itemView) {
    val textView: TextView

    init {
        textView = itemView.findViewById(R.id.lyrics_text)
    }

    override fun onBind(item: String) {
        textView.text = item
    }
}
