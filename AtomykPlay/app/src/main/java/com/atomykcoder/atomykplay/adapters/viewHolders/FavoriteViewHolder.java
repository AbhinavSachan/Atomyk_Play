package com.atomykcoder.atomykplay.adapters.viewHolders;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder;
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper;
import com.atomykcoder.atomykplay.models.Music;

public class FavoriteViewHolder extends GenericViewHolder<Music> {
    public final ImageView albumCoverIV;
    public final ImageView optBtn;
    public final View cardView;
    public final TextView nameText, artistText, durationText;

    public FavoriteViewHolder(@NonNull View itemView) {
        super(itemView);
        albumCoverIV = itemView.findViewById(R.id.song_album_cover_favorite);
        optBtn = itemView.findViewById(R.id.favorite_itemOpt);
        cardView = itemView.findViewById(R.id.cv_song_play_favorite);
        nameText = itemView.findViewById(R.id.song_name_favorite);
        artistText = itemView.findViewById(R.id.song_artist_name_favorite);
        durationText = itemView.findViewById(R.id.song_length_favorite);
    }

    @Override
    public void onBind(Music item) {
        nameText.setText(item.getName());
        artistText.setText(item.getArtist());
        durationText.setText(MusicHelper.convertDuration(item.getDuration()));
    }
}
