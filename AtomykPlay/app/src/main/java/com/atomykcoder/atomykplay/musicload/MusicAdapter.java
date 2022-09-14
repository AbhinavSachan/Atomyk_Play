package com.atomykcoder.atomykplay.musicload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewAdapter> {
    Context context;
    ArrayList<MusicDataCapsule> musicData;

    @NonNull
    @Override
    public MusicViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_item_layout, parent, false);
        return new MusicViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewAdapter holder, int position) {
        MusicDataCapsule currentItem = musicData.get(position);

        if (currentItem.getsCover() != null) {
            try {
                Glide.with(context).load(currentItem.getsCover()).apply(new RequestOptions().override(70, 70)).into(holder.imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                Glide.with(context).load(R.drawable.ic_music_list).apply(new RequestOptions().override(70, 70)).into(holder.imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.nameText.setText(currentItem.getsName());
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
        private final TextView nameText, artistText, durationText;

        public MusicViewAdapter(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.song_album_cover);
            imageButton = itemView.findViewById(R.id.more_option_i_btn);
            nameText = itemView.findViewById(R.id.song_name);
            artistText = itemView.findViewById(R.id.song_artist_name);
            durationText = itemView.findViewById(R.id.song_length);

        }
    }
}
