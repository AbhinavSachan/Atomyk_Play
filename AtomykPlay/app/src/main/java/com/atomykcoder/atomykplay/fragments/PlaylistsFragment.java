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

import java.util.ArrayList;

public class PlaylistsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<MusicDataCapsule> playlistList;
    private PlaylistAdapter playlistAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);

        recyclerView = view.findViewById(R.id.playlist_recycler_view);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacing(2, 8, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        playlistList = new StorageUtil(requireContext()).loadMusicList();

        if (playlistList != null) {
            playlistAdapter = new PlaylistAdapter(requireContext(), playlistList);
            recyclerView.setAdapter(playlistAdapter);
        }

        return view;
    }
}