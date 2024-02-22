package com.atomykcoder.atomykplay.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.generics.GenericRecyclerAdapter
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.viewHolders.BlackListViewHolder
import com.atomykcoder.atomykplay.ui.MainActivity
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage

class BlockFolderListAdapter(arrayList: ArrayList<String>?) :
    GenericRecyclerAdapter<String>() {
    private lateinit var context: Context
    private lateinit var settingsStorage: SettingsStorage

    init {
        super.items = arrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<String> {
        context = parent.context
        val view =
            LayoutInflater.from(context).inflate(R.layout.blacklist_item_layout, parent, false)
        return BlackListViewHolder(view)
    }

    override fun onBindViewHolder(_holder: GenericViewHolder<String>, position: Int) {
        super.onBindViewHolder(_holder, position)
        val holder = _holder as BlackListViewHolder
        holder.imageView.setOnClickListener { v: View? ->
            removeFromList(
                super.items!![position]!!
            )
        }
    }

    override fun getItemCount(): Int {
        return super.items!!.size
    }

    private fun removeFromList(name: String) {
        val pos = super.items!!.indexOf(name)
        val mainActivity = (context as MainActivity)
        settingsStorage = SettingsStorage(context)
        settingsStorage.removeFromBlackList(name)
        super.items!!.remove(name)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, super.items!!.size - (pos + 1))
        mainActivity.checkForUpdateList(true)
    }
}
