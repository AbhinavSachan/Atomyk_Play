package com.atomykcoder.atomykplay.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.PlaylistAdapter;
import com.atomykcoder.atomykplay.customScripts.GridSpacing;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.atomykcoder.atomykplay.viewModals.Playlist;

import java.util.ArrayList;

public class PlaylistsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<Playlist> playlistList;
    private PlaylistAdapter playlistAdapter;
    public View noPlLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);

        recyclerView = view.findViewById(R.id.playlist_recycler_view);
        noPlLayout = view.findViewById(R.id.no_pl_layout);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacing(2, 8, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        playlistList = new StorageUtil(requireContext()).getAllPlaylist();

        if (playlistList != null&&!playlistList.isEmpty()) {
            noPlLayout.setVisibility(View.GONE);
            playlistAdapter = new PlaylistAdapter(requireContext(), playlistList);
            recyclerView.setAdapter(playlistAdapter);
        }else {
            noPlLayout.setVisibility(View.VISIBLE);
        }

        return view;
    }
}