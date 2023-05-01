package com.atomykcoder.atomykplay.adapters.viewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder;

public class FoundLyricsViewHolder extends GenericViewHolder<String> {
    public TextView song_title;
    public TextView song_sampleLyrics;

    public FoundLyricsViewHolder(@NonNull View itemView) {
        super(itemView);
        song_title = itemView.findViewById(R.id.found_song_title);
        song_sampleLyrics = itemView.findViewById(R.id.found_song_lyrics_sample);
    }
}