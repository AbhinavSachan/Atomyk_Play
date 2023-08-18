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
import com.atomykcoder.atomykplay.ui.MainActivity
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.viewHolders.MusicQueueViewHolder
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.enums.OptionSheetEnum
import com.atomykcoder.atomykplay.helperFunctions.MusicDiffCallback
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.*

class MusicQueueAdapter(
    var context: Context,
    items: ArrayList<Music>?,
    onDragStartListener: OnDragStartListener
) : MusicAdapter(), ItemTouchHelperAdapter {
    private var onDragStartListener: OnDragStartListener
    var mainActivity: MainActivity
    private var storageUtil: StorageUtil
    private val glideBuilt: GlideBuilt = GlideBuilt(context.applicationContext)

    init {
        super.items = items
        this.onDragStartListener = onDragStartListener
        mainActivity = (context as MainActivity)
        storageUtil = StorageUtil(context)
    }

    @Synchronized
    fun updateMusicListItems(newMusicArrayList: ArrayList<Music>) {
        if (super.items == null) return
        super.items?.clear()
        super.items?.addAll(newMusicArrayList)
        notifyItemRangeChanged(0,newMusicArrayList.size)
    }

    //when item starts to move it will change positions of every item in real time
    @SuppressLint("NotifyDataSetChanged")
    override fun onItemMove(fromPos: Int, toPos: Int) {
        val savedIndex = storageUtil.loadMusicIndex()
        super.items?.let {
            Collections.swap(it, fromPos, toPos)
            storageUtil.saveQueueList(it) // Save the entire list after swapping
        }
        if (fromPos != toPos) {
            if (fromPos == savedIndex) {
                storageUtil.saveMusicIndex(toPos)
            } else if (toPos == savedIndex) {
                storageUtil.saveMusicIndex(fromPos)
            }
        }
        notifyDataSetChanged() // Notify the adapter that the dataset has changed
    }

    //removing item on swipe
    override fun onItemDismiss(position: Int) {
        //if any item has been removed this will save new list on temp list
        if (super.items != null) {
            if (position != -1 && position < super.items!!.size) {
                removeItem(super.items?.get(position))
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
        glideBuilt.loadAlbumArt(
            currentItem.path,
            R.drawable.ic_music,
            queueViewHolder.albumCoverIV,
            128,
            true
        )

        //playing song
        queueViewHolder.cardView.setOnClickListener {
            if (shouldIgnoreClick()) return@setOnClickListener
            if (!isMusicAvailable(currentItem)) {
                return@setOnClickListener
            }
            storageUtil.saveMusicIndex(position)
            mainActivity.playAudio(currentItem)
        }
        queueViewHolder.cardView.setOnLongClickListener {
            if (!isMusicAvailable(currentItem))return@setOnLongClickListener true
            mainActivity.openOptionMenu(currentItem,OptionSheetEnum.MAIN_LIST)
            true
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
        return super.items?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        val item = super.items?.get(position)
        return item?.id?.toLong() ?: 0
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
        item?.let {
            val position = super.items?.indexOf(it)
            if (position != null && position >= 0) {
                super.items!!.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, super.items!!.size - position)
                storageUtil.saveQueueList(super.items!!)

                if (super.items!!.isEmpty()) {
                    mainActivity.bottomSheetPlayerFragment?.setQueueSheetBehaviour(BottomSheetBehavior.STATE_HIDDEN)
                    mainActivity.mainPlayerSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                    mainActivity.bottomSheetPlayerFragment?.resetMainPlayerLayout()
                    mainActivity.resetDataInNavigation()
                    mainActivity.stopMusic()
                    mainActivity.clearStorage()
                } else {
                    val savedIndex = storageUtil.loadMusicIndex()
                    if (position == savedIndex) {
                        if (savedIndex == super.items!!.size) {
                            mainActivity.playAudio(super.items!![savedIndex - 1])
                            storageUtil.saveMusicIndex(savedIndex - 1)
                        }else{
                            mainActivity.playAudio(super.items!![savedIndex])
                        }
                    } else if (position < savedIndex) {
                        storageUtil.saveMusicIndex(savedIndex - 1)
                    }
                }
            }
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