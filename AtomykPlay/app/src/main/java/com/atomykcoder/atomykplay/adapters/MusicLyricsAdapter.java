package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.Generics.GenericRecyclerAdapter;
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder;
import com.atomykcoder.atomykplay.adapters.ViewHolders.MusicLyricsViewHolder;

import java.util.ArrayList;

public class MusicLyricsAdapter extends GenericRecyclerAdapter<String> {
    private final Context context;

    public MusicLyricsAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        super.items = arrayList;
    }

    @NonNull
    @Override
    public GenericViewHolder<String> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lyrics_item, parent, false);
        return new MusicLyricsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<String> _holder, int position) {
        super.onBindViewHolder(_holder, position);

        MusicLyricsViewHolder holder = (MusicLyricsViewHolder) _holder;


        holder.textView.setOnClickListener(v -> ((MainActivity) context).bottomSheetPlayerFragment
                .skipToPosition(holder.getAbsoluteAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return super.items.size();
    }


}

