package com.atomykcoder.atomykplay.function;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.MainActivity;
import com.atomykcoder.atomykplay.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewAdapter> {
    Context context;
    ArrayList<MusicDataCapsule> musicData;


    public MusicAdapter(Context context, ArrayList<MusicDataCapsule> musicData) {
        this.context = context;
        this.musicData = musicData;
    }

    @NonNull
    @Override
    public MusicViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_item_layout, parent, false);
        return new MusicViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewAdapter holder, int position) {
        MusicDataCapsule currentItem = musicData.get(position);

        if (currentItem.getsAlbumUri() != null) {
            try {
                Glide.with(context).load(currentItem.getsAlbumUri()).apply(new RequestOptions()
                        .override(150, 150)).into(holder.imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //playing song
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.playAudio(position);
            }
        });


        //add bottom sheet functions in three dot click
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        String sName = currentItem.getsName()
                .replace("y2mate.com - ", "")
                .replace("&#039;", "'")
                .replace("%20", " ")
                .replace("_", " ")
                .replace("&amp;", ",");

        holder.nameText.setText(sName);
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(currentItem.getsLength());
    }


    @Override
    public int getItemCount() {
        return musicData.size();
    }

    public static class MusicViewAdapter extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageButton imageButton;
        private final MaterialCardView cardView;
        private final TextView nameText, artistText, durationText;

        public MusicViewAdapter(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.song_album_cover);
            imageButton = itemView.findViewById(R.id.more_option_i_btn);
            cardView = itemView.findViewById(R.id.cv_song_play);
            nameText = itemView.findViewById(R.id.song_name);
            artistText = itemView.findViewById(R.id.song_artist_name);
            durationText = itemView.findViewById(R.id.song_length);

        }
    }
}
