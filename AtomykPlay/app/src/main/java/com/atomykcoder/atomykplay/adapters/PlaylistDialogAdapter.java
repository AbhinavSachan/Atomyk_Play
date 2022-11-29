package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.atomykcoder.atomykplay.viewModals.Playlist;

import java.util.ArrayList;

public class PlaylistDialogAdapter extends RecyclerView.Adapter<PlaylistDialogAdapter.PlaylistViewHolder> {
    private Context context;
    private ArrayList<Playlist> playlists;
    private MusicDataCapsule selectedMusic;

    public PlaylistDialogAdapter(Context context, ArrayList<Playlist> playlists, MusicDataCapsule selectedMusic) {
        this.context = context;
        this.playlists = playlists;
        this.selectedMusic = selectedMusic;
    }
    @NonNull
    @Override
    public PlaylistDialogAdapter.PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.playlist_dialog_item_layout, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistDialogAdapter.PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        MainActivity mainActivity = (MainActivity) context;
        StorageUtil storageUtil = new StorageUtil(context);
        holder.textView.setText(playlist.getName());
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storageUtil.addItemInPlayList(selectedMusic,playlist.getName());
                mainActivity.addToPlDialog.cancel();
                Toast.makeText(context,"added to "+playlist.getName(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final View view;
        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            textView  =itemView.findViewById(R.id.playlist_name_dialog_tv);
            view  =itemView.findViewById(R.id.playlist_name_dialog_ll);
        }
    }
}
