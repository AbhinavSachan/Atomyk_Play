package com.atomykcoder.atomykplay.adapters.viewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder;
import com.atomykcoder.atomykplay.dataModels.Playlist;

public class PlaylistViewHolder extends GenericViewHolder<Playlist> {
    public TextView playlistName;
    public TextView songCount;
    public ImageView coverIV, optImg;
    public View cardView;

    public PlaylistViewHolder(@NonNull View view) {
        super(view);
        playlistName = view.findViewById(R.id.playlist_name_tv);
        songCount = view.findViewById(R.id.playlist_item_count_tv);
        coverIV = view.findViewById(R.id.playlist_cover_img);
        optImg = view.findViewById(R.id.playlist_option);
        cardView = view.findViewById(R.id.playlist_card);
    }

}
