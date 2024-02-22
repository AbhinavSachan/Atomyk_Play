package com.atomykcoder.atomykplay.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.viewHolders.FoundLyricsViewHolder
import com.atomykcoder.atomykplay.constants.FragmentTags.ADD_LYRICS_FRAGMENT_TAG
import com.atomykcoder.atomykplay.fragments.AddLyricsFragment
import com.atomykcoder.atomykplay.ui.MainActivity

class FoundLyricsAdapter(
    private val titles: ArrayList<String>?,
    private val sampleLyrics: ArrayList<String>?,
    private val urls: ArrayList<String>?,
    _context: Context
) : RecyclerView.Adapter<FoundLyricsViewHolder>() {
    private val mainActivity: MainActivity

    init {
        mainActivity = _context as MainActivity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoundLyricsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.found_lyrics_item, parent, false)
        return FoundLyricsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoundLyricsViewHolder, position: Int) {
        holder.song_title.text = titles?.get(position)
        holder.song_sampleLyrics.text = sampleLyrics?.get(position)
        holder.itemView.setOnClickListener { view: View? ->
            mainActivity.setBottomSheetState()
            val fragment =
                mainActivity.supportFragmentManager.findFragmentByTag(ADD_LYRICS_FRAGMENT_TAG) as AddLyricsFragment?
            fragment?.loadSelectedLyrics(urls?.get(holder.bindingAdapterPosition))
        }
    }

    override fun getItemCount(): Int {
        return titles?.size ?:0
    }
}
