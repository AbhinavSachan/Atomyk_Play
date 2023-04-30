package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.activities.MainActivity.OPEN_PLAYLIST_FRAGMENT_TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.generics.GenericRecyclerAdapter;
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder;
import com.atomykcoder.atomykplay.adapters.viewHolders.PlaylistViewHolder;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.dataModels.Playlist;
import com.atomykcoder.atomykplay.fragments.OpenPlayListFragment;

import java.util.ArrayList;

public class PlaylistAdapter extends GenericRecyclerAdapter<Playlist> {
    private final Context context;
    private final GlideBuilt glideBuilt;

    public PlaylistAdapter(Context context, ArrayList<Playlist> arrayList) {
        this.context = context;
        super.items = arrayList;
        glideBuilt = new GlideBuilt(context);
    }

    @NonNull
    @Override
    public GenericViewHolder<Playlist> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item_layout, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<Playlist> _holder, int position) {

        PlaylistViewHolder holder = (PlaylistViewHolder) _holder;

        Playlist currentItem = super.items.get(position);

        ArrayList<Music> musicList = currentItem.getMusicList();

        glideBuilt.glide(currentItem.getCoverUri(), R.drawable.ic_music_list, holder.coverIV, 412);
        String count = musicList.size() + " Songs";
        holder.playlistName.setText(currentItem.getName());
        holder.songCount.setText(count);

        holder.cardView.setOnClickListener(v -> {
            //opening fragment when clicked on playlist
            FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();

            Fragment fragment3 = fragmentManager.findFragmentByTag(OPEN_PLAYLIST_FRAGMENT_TAG);
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putSerializable("currentPlaylist", currentItem);

            if (fragment3 != null) {
                fragmentManager.popBackStackImmediate();
            }
            OpenPlayListFragment openPlayListFragment = new OpenPlayListFragment();
            openPlayListFragment.setArguments(bundle);

            transaction.add(R.id.sec_container, openPlayListFragment, OPEN_PLAYLIST_FRAGMENT_TAG).addToBackStack(null).commit();

        });

        holder.optImg.setOnClickListener(v -> ((MainActivity) context).openPlOptionMenu(currentItem));

    }

    @Override
    public int getItemCount() {
        return super.items.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateView(ArrayList<Playlist> _arrayList) {
        super.items.clear();
        super.items.addAll(_arrayList);
        notifyDataSetChanged();
    }

}

