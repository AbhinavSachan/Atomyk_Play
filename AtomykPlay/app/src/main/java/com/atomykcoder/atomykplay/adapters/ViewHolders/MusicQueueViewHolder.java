package com.atomykcoder.atomykplay.adapters.ViewHolders;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder;
import com.atomykcoder.atomykplay.data.Music;

public class MusicQueueViewHolder extends GenericViewHolder<Music> {
    public final ImageView albumCoverIV;
    public final ImageView dragButton;
    public final View cardView;
    public final TextView nameText, artistText, durationText, musicIndex;

    public MusicQueueViewHolder(@NonNull View itemView) {
        super(itemView);
        albumCoverIV = itemView.findViewById(R.id.song_album_cover_queue);
        dragButton = itemView.findViewById(R.id.drag_i_btn_queue);
        cardView = itemView.findViewById(R.id.cv_song_play_queue);
        nameText = itemView.findViewById(R.id.song_name_queue);
        musicIndex = itemView.findViewById(R.id.song_index_num_queue);
        artistText = itemView.findViewById(R.id.song_artist_name_queue);
        durationText = itemView.findViewById(R.id.song_length_queue);
    }

    @Override
    public void onBind(Music item) {
        nameText.setText(item.getName());
        artistText.setText(item.getArtist());
        durationText.setText(convertDuration(item.getDuration()));

    }
}
