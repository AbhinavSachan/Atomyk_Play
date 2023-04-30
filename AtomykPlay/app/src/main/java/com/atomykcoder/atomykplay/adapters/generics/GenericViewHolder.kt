package com.atomykcoder.atomykplay.adapters.generics

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.atomykcoder.atomykplay.interfaces.IBindableViewHolder

open class GenericViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(
    itemView
), IBindableViewHolder<T> {
    override fun onBind(item: T) {}
}