package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.fragments.OpenPlayListFragment;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.atomykcoder.atomykplay.viewModals.Playlist;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private final Context context;
    private final ArrayList<Playlist> arrayList;

    public PlaylistAdapter(Context context, ArrayList<Playlist> arrayList) {
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
        Playlist currentItem = arrayList.get(position);
        ArrayList<MusicDataCapsule> musicList = currentItem.getMusicArrayList();

        GlideBuilt.glide(context, currentItem.getCoverUri(), R.drawable.ic_music_list, holder.imageView, 300);
        String count = musicList.size() + " Songs";
        holder.playlistName.setText(currentItem.getName());
        holder.songCount.setText(count);

        holder.cardView.setOnClickListener(v -> {
            //opening fragment when clicked on playlist
            FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();

            Fragment fragment3 = fragmentManager.findFragmentByTag("OpenPlayListFragment");

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putSerializable("currentPlaylist", currentItem);

            if (fragment3 != null) {
                fragmentManager.popBackStackImmediate();
            }

            OpenPlayListFragment fragment = new OpenPlayListFragment();
            fragment.setArguments(bundle);
            fragment.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_bottom));

            transaction.add(R.id.sec_container, fragment, "OpenPlayListFragment").addToBackStack(null).commit();

        });

        holder.optImg.setOnClickListener(v -> ((MainActivity) context).openPlOptionMenu(currentItem));

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final TextView playlistName;
        private final TextView songCount;
        private final ImageView imageView, optImg;
        private final View cardView;

        public PlaylistViewHolder(@NonNull View view) {
            super(view);
            playlistName = view.findViewById(R.id.playlist_name_tv);
            songCount = view.findViewById(R.id.playlist_item_count_tv);
            imageView = view.findViewById(R.id.playlist_cover_img);
            optImg = view.findViewById(R.id.playlist_option);
            cardView = view.findViewById(R.id.playlist_card);
        }
    }
}
