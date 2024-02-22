package com.atomykcoder.atomykplay.adapters.viewHolders

import android.view.View
import android.widget.TextView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.models.Playlist

class PlayListDialogViewHolder(itemView: View) : GenericViewHolder<Playlist>(itemView) {
    val textView: TextView
    val view: View

    init {
        textView = itemView.findViewById(R.id.playlist_name_dialog_tv)
        view = itemView.findViewById(R.id.playlist_name_dialog_ll)
    }
}
