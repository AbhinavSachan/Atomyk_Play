package com.atomykcoder.atomykplay.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericRecyclerAdapter
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.viewHolders.MusicLyricsViewHolder
import com.atomykcoder.atomykplay.ui.MainActivity

class MusicLyricsAdapter(private val context: Context, arrayList: ArrayList<String>?) :
    GenericRecyclerAdapter<String>() {
    init {
        super.items = arrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<String> {
        val view = LayoutInflater.from(context).inflate(R.layout.lyrics_item, parent, false)
        return MusicLyricsViewHolder(view)
    }

    override fun onBindViewHolder(_holder: GenericViewHolder<String>, position: Int) {
        super.onBindViewHolder(_holder, position)
        val holder = _holder as MusicLyricsViewHolder
        holder.textView.setOnClickListener {
            (context as? MainActivity)?.bottomSheetPlayerFragment?.skipToPosition(holder.absoluteAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return super.items?.size ?: 0
    }
}
