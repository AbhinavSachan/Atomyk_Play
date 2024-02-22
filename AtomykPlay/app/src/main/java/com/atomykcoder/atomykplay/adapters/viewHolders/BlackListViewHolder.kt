package com.atomykcoder.atomykplay.adapters.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder

class BlackListViewHolder(itemView: View) : GenericViewHolder<String>(itemView) {
    val imageView: ImageView
    private val textView: TextView

    init {
        textView = itemView.findViewById(R.id.blacklist_directory_path)
        imageView = itemView.findViewById(R.id.delete_from_blacklist)
    }

    override fun onBind(item: String) {
        textView.text = item
    }
}
