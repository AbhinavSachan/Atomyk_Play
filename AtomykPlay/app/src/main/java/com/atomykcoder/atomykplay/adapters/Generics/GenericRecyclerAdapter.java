package com.atomykcoder.atomykplay.adapters.Generics;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GenericRecyclerAdapter<T> extends RecyclerView.Adapter<GenericViewHolder<T>> {

    protected ArrayList<T> items;
    protected int delay = 600;
    long lastClickTime;

    @SuppressLint("NotifyDataSetChanged")
    public void addItems(ArrayList<T> _items) {
        items.addAll(_items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GenericViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(viewType, parent, false);
        return new GenericViewHolder<T>(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<T> holder, int position) {
        holder.onBind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected boolean shouldIgnoreClick() {
        if (SystemClock.elapsedRealtime() < (lastClickTime + delay)) {
            return true;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        return false;
    }
}
