package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.generics.GenericRecyclerAdapter;
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder;
import com.atomykcoder.atomykplay.adapters.viewHolders.PlayListDialogViewHolder;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.dataModels.Playlist;
import com.atomykcoder.atomykplay.utils.StorageUtil;

import java.util.ArrayList;

public class PlaylistDialogAdapter extends GenericRecyclerAdapter<Playlist> {
    private final Context context;
    private final Music music;

    public PlaylistDialogAdapter(Context _context, ArrayList<Playlist> _playlists, Music _music) {
        context = _context;
        super.items = _playlists;
        music = _music;
    }

    @NonNull
    @Override
    public GenericViewHolder<Playlist> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.playlist_dialog_item_layout, parent, false);
        return new PlayListDialogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<Playlist> _holder, int position) {
        PlayListDialogViewHolder holder = (PlayListDialogViewHolder) _holder;

        Playlist playlist = super.items.get(position);
        MainActivity mainActivity = (MainActivity) context;
        StorageUtil storageUtil = new StorageUtil(context);
        holder.textView.setText(playlist.getName());
        holder.view.setOnClickListener(v -> {
            storageUtil.saveItemInPlayList(music, playlist.getName());
            mainActivity.addToPlDialog.dismiss();
            Toast.makeText(context, "added to " + playlist.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return super.items.size();
    }

}

