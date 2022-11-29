package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private final Context context;
    private final ArrayList<MusicDataCapsule> arrayList;

    public PlaylistAdapter(Context context, ArrayList<MusicDataCapsule> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public PlaylistAdapter.PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item_layout, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistAdapter.PlaylistViewHolder holder, int position) {
        MusicDataCapsule capsule = arrayList.get(position);
        GlideBuilt.glide(context,capsule.getsAlbumUri(),R.drawable.ic_music,holder.imageView,300);
        String count = arrayList.size() + " Songs";
        holder.playlistName.setText(capsule.getsName());
        holder.songCount.setText(count);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final TextView playlistName;
        private final TextView songCount;
        private final ImageView imageView;
        private final MaterialCardView cardView;
        public PlaylistViewHolder(@NonNull View view) {
            super(view);
            playlistName = view.findViewById(R.id.playlist_name_tv);
            songCount = view.findViewById(R.id.playlist_item_count_tv);
            imageView = view.findViewById(R.id.playlist_cover_img);
            cardView = view.findViewById(R.id.playlist_card);
        }
    }
}
