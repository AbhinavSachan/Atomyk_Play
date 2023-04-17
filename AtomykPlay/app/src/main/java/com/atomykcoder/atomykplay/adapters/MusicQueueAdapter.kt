package com.atomykcoder.atomykplay.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.activities.MainActivity
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.ViewHolders.MusicQueueViewHolder
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.helperFunctions.MusicDiffCallback
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener
import com.atomykcoder.atomykplay.kotlin.ImageLoader
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.*

class MusicQueueAdapter(
    var context: Context,
    items: ArrayList<Music>?,
    onDragStartListener: OnDragStartListener
) : MusicAdapter(), ItemTouchHelperAdapter {
    var onDragStartListener: OnDragStartListener
    var mainActivity: MainActivity
    var storageUtil: StorageUtil
    var imageLoader: ImageLoader

    init {
        super.items = items
        this.onDragStartListener = onDragStartListener
        mainActivity = context as MainActivity
        storageUtil = StorageUtil(context)
        imageLoader = ImageLoader(context)
    }

    @Synchronized
    fun updateMusicListItems(newMusicArrayList: ArrayList<Music>?) {
        if (super.items == null) return
        val diffCallback = MusicDiffCallback(super.items!!, newMusicArrayList!!)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        super.items!!.clear()
        super.items!!.addAll(newMusicArrayList)
        diffResult.dispatchUpdatesTo(this)
    }

    //when item starts to move it will change positions of every item in real time
    override fun onItemMove(fromPos: Int, toPos: Int) {
        val savedIndex = storageUtil.loadMusicIndex()
        super.items?.let { Collections.swap(it, fromPos, toPos) }
        notifyItemMoved(fromPos, toPos)
        notifyItemRangeChanged(fromPos, 1, null)
        notifyItemChanged(toPos, null)
        if (fromPos < savedIndex) {
            if (toPos == savedIndex || toPos > savedIndex) {
                storageUtil.saveMusicIndex(savedIndex - 1)
            }
        } else if (fromPos > savedIndex) {
            if (toPos == savedIndex || toPos < savedIndex) {
                storageUtil.saveMusicIndex(savedIndex + 1)
            }
        } else {
            storageUtil.saveMusicIndex(toPos)
        }
        storageUtil.saveQueueList(super.items!!)
    }

    //removing item on swipe
    override fun onItemDismiss(position: Int) {
        //if any item has been removed this will save new list on temp list
        if (super.items != null) {
            if (position != -1 && position < super.items!!.size) {
                removeItem(super.items!![position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<Music> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.queue_music_item_layout, parent, false)
        return MusicQueueViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: GenericViewHolder<Music>, position: Int) {
        super.onBindViewHolder(holder, position)
        val queueViewHolder = holder as MusicQueueViewHolder
        val currentItem = super.items!![position]
        val index = "${position + 1}"
        queueViewHolder.musicIndex.text = index
        imageLoader.loadImage(R.drawable.ic_music, currentItem, queueViewHolder.albumCoverIV, 128)

        //playing song
        queueViewHolder.cardView.setOnClickListener {
            if (shouldIgnoreClick()) return@setOnClickListener
            if (!isMusicAvailable(currentItem)) {
                return@setOnClickListener
            }
            storageUtil.saveMusicIndex(position)
            mainActivity.playAudio(currentItem)
        }
        queueViewHolder.dragButton.setOnTouchListener { _: View?, event: MotionEvent ->
            if (!isMusicAvailable(currentItem)) {
                return@setOnTouchListener false
            }
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                onDragStartListener.onDragStart(queueViewHolder)
            }
            false
        }
    }

    override fun getItemCount(): Int {
        return super.items?.size ?:0
    }

    private fun isMusicAvailable(currentItem: Music): Boolean {
        if (!doesMusicExists(currentItem)) {
            Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show()
            removeItem(currentItem)
            return false
        }
        return true
    }

    fun removeItem(item: Music?) {
        val position = super.items!!.indexOf(item)
        val savedIndex = storageUtil.loadMusicIndex()
        if (super.items!!.isNotEmpty()) {
            if (super.items!!.size == 1) {
                mainActivity.bottomSheetPlayerFragment?.queueSheetBehaviour?.state =
                    BottomSheetBehavior.STATE_HIDDEN
                mainActivity.clearStorage()
                clearList()
                mainActivity.mainPlayerSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                mainActivity.bottomSheetPlayerFragment?.resetMainPlayerLayout()
                mainActivity.resetDataInNavigation()
                mainActivity.stopMusic()
            }
            if (item != null) {
                super.items!!.remove(item)
            }
            if (super.items!!.isNotEmpty()) {
                if (position == savedIndex) {
                    if (savedIndex != -1) {
                        mainActivity.playAudio(super.items!![savedIndex])
                    }
                } else if (position < savedIndex) {
                    storageUtil.saveMusicIndex(savedIndex - 1)
                }
            }
            notifyItemRangeChanged(position, super.items!!.size - (position + 1))
            notifyItemRemoved(position)
            storageUtil.saveQueueList(super.items!!)
        }
    }

    fun clearList() {
        super.items!!.clear()
    }

    fun updateItemInserted(music: Music?) {
        val pos = storageUtil.loadMusicIndex()
        if (super.items!!.isEmpty()) {
            super.items!!.add(0, music)
            storageUtil.saveMusicIndex(0)
            mainActivity.playAudio(music)
            mainActivity.openBottomPlayer()
            notifyItemInserted(0)
        } else {
            super.items!!.add(pos + 1, music)
            notifyItemInserted(pos + 1)
            notifyItemRangeChanged(pos + 1, super.items!!.size - (pos + 2))
        }
        storageUtil.saveQueueList(super.items!!)
    }

    fun updateListInserted(list: ArrayList<Music?>) {
        val pos = storageUtil.loadMusicIndex()
        if (super.items!!.isEmpty()) {
            super.items!!.addAll(0, list)
            storageUtil.saveMusicIndex(0)
            mainActivity.playAudio(list[0])
            mainActivity.openBottomPlayer()
            notifyItemRangeInserted(1, list.size)
            notifyItemRangeChanged(list.size + 1, super.items!!.size - (pos + list.size + 2))
        } else {
            super.items!!.addAll(pos + 1, list)
            notifyItemRangeInserted(pos + 1, list.size)
            notifyItemRangeChanged(pos + list.size + 1, super.items!!.size - (pos + list.size + 2))
        }
        storageUtil.saveQueueList(super.items!!)
    }

    fun updateListInsertedLast(list: ArrayList<Music?>) {
        if (super.items!!.isEmpty()) {
            storageUtil.saveMusicIndex(0)
            mainActivity.playAudio(list[0])
            mainActivity.openBottomPlayer()
        }
        super.items!!.addAll(list)
        val pos = super.items!!.lastIndexOf(list[0])
        notifyItemRangeInserted(pos, list.size)
        storageUtil.saveQueueList(super.items!!)
    }

    fun updateItemInsertedLast(music: Music?) {
        if (super.items!!.isEmpty()) {
            storageUtil.saveMusicIndex(0)
            mainActivity.playAudio(music)
            mainActivity.openBottomPlayer()
        }
        super.items!!.add(music)
        val pos = super.items!!.lastIndexOf(music)
        notifyItemInserted(pos)
        storageUtil.saveQueueList(super.items!!)
    }
}