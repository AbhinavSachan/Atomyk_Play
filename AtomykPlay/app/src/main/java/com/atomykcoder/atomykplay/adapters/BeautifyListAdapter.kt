package com.atomykcoder.atomykplay.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.activities.MainActivity
import com.atomykcoder.atomykplay.adapters.Generics.GenericRecyclerAdapter
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder
import com.atomykcoder.atomykplay.adapters.ViewHolders.BeautifyListViewHolder
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage

class BeautifyListAdapter(
    tags: ArrayList<String?>?,
    context: Context,
    private val replacingTags: ArrayList<String>,
) : GenericRecyclerAdapter<String?>() {
    private val context: Context
    var settingsStorage: SettingsStorage

    init {
        super.items = tags
        this.context = context
        settingsStorage = SettingsStorage(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<String?> {
        val view =
            LayoutInflater.from(context).inflate(R.layout.blacklist_item_layout, parent, false)
        return BeautifyListViewHolder(view)
    }

    override fun onBindViewHolder(_holder: GenericViewHolder<String?>, position: Int) {
        super.onBindViewHolder(_holder, position)
        val holder = _holder as BeautifyListViewHolder
        val s: String = super.items!![position] + " = " + "(" + replacingTags[position] + ")"
        holder.textView.text = s
        holder.imageView.setOnClickListener { removeFromList(position) }
    }

    override fun getItemCount(): Int {
        return super.items!!.size
    }

    private fun removeFromList(pos: Int) {
        val mainActivity = context as MainActivity
        settingsStorage.removeFromBeautifyList(pos.toString())
        settingsStorage.removeFromReplacingList(pos.toString())
        super.items?.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, super.items!!.size - (pos + 1))
        mainActivity.checkForUpdateList(true)
    }
}