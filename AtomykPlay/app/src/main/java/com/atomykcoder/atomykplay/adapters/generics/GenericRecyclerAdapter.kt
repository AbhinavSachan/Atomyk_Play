package com.atomykcoder.atomykplay.adapters.generics

import android.annotation.SuppressLint
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

open class GenericRecyclerAdapter<T> : RecyclerView.Adapter<GenericViewHolder<T>>() {
    @JvmField
    protected var items: ArrayList<T>? = null
    private var delay = 600
    private var lastClickTime: Long = 0
    @SuppressLint("NotifyDataSetChanged")
    fun addItems(_items: ArrayList<T>?) {
        items?.addAll(_items!!)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewType, parent, false)
        return GenericViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenericViewHolder<T>, position: Int) {
        holder.onBind(items!![position])
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    protected fun shouldIgnoreClick(): Boolean {
        if (SystemClock.elapsedRealtime() < lastClickTime + delay) {
            return true
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return false
    }
}