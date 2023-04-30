package com.atomykcoder.atomykplay.adapters.viewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder;

public class MusicLyricsViewHolder extends GenericViewHolder<String> {
    public final TextView textView;

    public MusicLyricsViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.lyrics_text);
    }

    @Override
    public void onBind(String item) {
        textView.setText(item);
    }
}
