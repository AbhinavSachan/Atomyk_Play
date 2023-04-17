package com.atomykcoder.atomykplay.helperFunctions

import androidx.recyclerview.widget.DiffUtil
import com.atomykcoder.atomykplay.data.Music

class MusicDiffCallback(
    private val oldMusicList: ArrayList<Music>?,
    private val newMusicList: ArrayList<Music>?
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldMusicList?.size ?:0
    }

    override fun getNewListSize(): Int {
        return newMusicList?.size ?:0
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMusicList?.get(oldItemPosition)?.id == newMusicList?.get(newItemPosition)?.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMusic = oldMusicList?.get(oldItemPosition)
        val newMusic = newMusicList?.get(newItemPosition)
        return oldMusic === newMusic
    }
}