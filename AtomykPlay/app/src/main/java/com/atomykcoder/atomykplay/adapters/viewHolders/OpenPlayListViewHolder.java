package com.atomykcoder.atomykplay.adapters.viewHolders;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder;
import com.atomykcoder.atomykplay.data.Music;

public class OpenPlayListViewHolder extends GenericViewHolder<Music> {
    public final ImageView albumCoverIV;
    public final ImageView optBtn;
    public final View cardView;
    public final TextView nameText, artistText, durationText;

    public OpenPlayListViewHolder(@NonNull View itemView) {
        super(itemView);
        albumCoverIV = itemView.findViewById(R.id.song_album_cover_opl);
        optBtn = itemView.findViewById(R.id.playlist_itemOpt);
        cardView = itemView.findViewById(R.id.cv_song_play_opl);
        nameText = itemView.findViewById(R.id.song_name_opl);
        artistText = itemView.findViewById(R.id.song_artist_name_opl);
        durationText = itemView.findViewById(R.id.song_length_opl);
    }

    @Override
    public void onBind(Music item) {
        nameText.setText(item.getName());
        artistText.setText(item.getArtist());
        durationText.setText(convertDuration(item.getDuration()));
    }
}
