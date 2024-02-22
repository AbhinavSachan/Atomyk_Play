package com.atomykcoder.atomykplay.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericRecyclerAdapter
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.viewHolders.PlaylistViewHolder
import com.atomykcoder.atomykplay.constants.FragmentTags.OPEN_PLAYLIST_FRAGMENT_TAG
import com.atomykcoder.atomykplay.fragments.OpenPlayListFragment.Companion.newInstance
import com.atomykcoder.atomykplay.models.Playlist
import com.atomykcoder.atomykplay.ui.MainActivity
import com.atomykcoder.atomykplay.utils.loadImageFromUri

class PlaylistAdapter(context: Context, arrayList: ArrayList<Playlist>?) :
    GenericRecyclerAdapter<Playlist>() {
    private val mainActivity: MainActivity

    init {
        super.items = arrayList
        mainActivity = context as MainActivity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<Playlist> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.playlist_item_layout, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(_holder: GenericViewHolder<Playlist>, position: Int) {
        val holder = _holder as PlaylistViewHolder
        val currentItem = super.items!![position]!!
        val musicList = currentItem.musicList
        holder.coverIV.loadImageFromUri(currentItem.coverUri, R.drawable.ic_music_list, 412)
        val count = musicList!!.size.toString() + " Songs"
        holder.playlistName.text = currentItem.name
        holder.songCount.text = count
        holder.cardView.setOnClickListener { v: View? ->
            //opening fragment when clicked on playlist
            val fragmentManager = mainActivity.supportFragmentManager
            val fragment3 = fragmentManager.findFragmentByTag(OPEN_PLAYLIST_FRAGMENT_TAG)
            val transaction = fragmentManager.beginTransaction()
            if (fragment3 != null) {
                fragmentManager.popBackStackImmediate()
            }
            val openPlayListFragment = newInstance(currentItem)
            transaction.add(R.id.sec_container, openPlayListFragment, OPEN_PLAYLIST_FRAGMENT_TAG)
                .addToBackStack(null).commit()
        }
        holder.optImg.setOnClickListener { v: View? -> mainActivity.openPlOptionMenu(currentItem) }
    }

    override fun getItemCount(): Int {
        return if (super.items != null) super.items!!.size else 0
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateView(_arrayList: ArrayList<Playlist?>) {
        if (super.items != null) {
            super.items!!.clear()
            super.items!!.addAll(_arrayList.filterNotNull())
        }
        notifyDataSetChanged()
    }
}
