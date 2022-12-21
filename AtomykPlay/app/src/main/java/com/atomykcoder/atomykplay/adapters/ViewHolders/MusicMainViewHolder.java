package com.atomykcoder.atomykplay.adapters.ViewHolders;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder;
import com.atomykcoder.atomykplay.data.Music;

public class MusicMainViewHolder extends GenericViewHolder<Music> {
    public final ImageView albumCoverIV;
    public final ImageView optionButton;
    public final RelativeLayout cardView;
    public final TextView nameText, artistText, durationText;

    public MusicMainViewHolder(@NonNull View itemView) {
        super(itemView);
        albumCoverIV = itemView.findViewById(R.id.song_album_cover);
        optionButton = itemView.findViewById(R.id.option);
        cardView = itemView.findViewById(R.id.cv_song_play);
        nameText = itemView.findViewById(R.id.song_name);
        artistText = itemView.findViewById(R.id.song_artist_name);
        durationText = itemView.findViewById(R.id.song_length);

    }

    @Override
    public void onBind(Music item) {
        nameText.setText(item.getName());
        artistText.setText(item.getArtist());
        durationText.setText(convertDuration(item.getDuration()));
    }
}
