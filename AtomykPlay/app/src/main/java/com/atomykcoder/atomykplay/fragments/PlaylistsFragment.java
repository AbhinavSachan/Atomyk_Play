package com.atomykcoder.atomykplay.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.PlaylistAdapter;
import com.atomykcoder.atomykplay.customScripts.GridSpacing;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.viewModals.Playlist;

import java.util.ArrayList;

public class PlaylistsFragment extends Fragment {

    public View noPlLayout;
    private RecyclerView recyclerView;
    private ArrayList<Playlist> playlistList;
    private PlaylistAdapter playlistAdapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);

        recyclerView = view.findViewById(R.id.playlist_recycler_view);
        noPlLayout = view.findViewById(R.id.no_pl_layout);

        View favBtn = view.findViewById(R.id.pl_favorites_btn);

        Toolbar toolbar = view.findViewById(R.id.toolbar_playlists);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                Fragment fragment1 = fragmentManager.findFragmentByTag("FavoritesFragment");
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                if (fragment1 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                FavoritesFragment fragment = new FavoritesFragment();
                fragment.setEnterTransition(TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.slide_bottom));

                transaction.add(R.id.sec_container, fragment, "FavoritesFragment").addToBackStack(null).commit();
            }
        });

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacing(2, 8, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        playlistList = new StorageUtil(requireContext()).getAllPlaylist();

        if (playlistList != null && !playlistList.isEmpty()) {
            noPlLayout.setVisibility(View.GONE);
            if (playlistList.isEmpty()) {
                noPlLayout.setVisibility(View.VISIBLE);
            }
            playlistAdapter = new PlaylistAdapter(getContext(), playlistList);
            recyclerView.setAdapter(playlistAdapter);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        noPlLayout = null;
        super.onDestroyView();
    }
}