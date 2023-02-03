package com.atomykcoder.atomykplay.adapters.Generics;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.interfaces.IBindableViewHolder;

public class GenericViewHolder<T> extends RecyclerView.ViewHolder implements IBindableViewHolder<T> {
    public GenericViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(T item) {
    }
}
