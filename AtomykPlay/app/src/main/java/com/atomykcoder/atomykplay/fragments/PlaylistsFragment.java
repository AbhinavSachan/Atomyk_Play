package com.atomykcoder.atomykplay.fragments;

import static com.atomykcoder.atomykplay.activities.MainActivity.FAVORITE_FRAGMENT_TAG;

import android.os.Bundle;
import android.transition.TransitionInflater;
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

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.PlaylistAdapter;
import com.atomykcoder.atomykplay.customScripts.GridSpacing;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.dataModels.Playlist;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class PlaylistsFragment extends Fragment {

    public View noPlLayout;
    public ArrayList<Playlist> playlistList;
    public PlaylistAdapter playlistAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.playlist_recycler_view);
        noPlLayout = view.findViewById(R.id.no_pl_layout);

        View favBtn = view.findViewById(R.id.pl_favorites_btn);

        Toolbar toolbar = view.findViewById(R.id.toolbar_playlists);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        favBtn.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            Fragment fragment1 = fragmentManager.findFragmentByTag(FAVORITE_FRAGMENT_TAG);
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (fragment1 == null) {
                FavoritesFragment fragment = new FavoritesFragment();
                fragment.setEnterTransition(TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.slide_right));
                transaction.add(R.id.sec_container, fragment, FAVORITE_FRAGMENT_TAG).addToBackStack(null).commit();
            }
        });

        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacing(2, 8, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        playlistList = new StorageUtil(requireContext()).getAllPlaylist();

        if (playlistList != null) {
            noPlLayout.setVisibility(View.GONE);
            if (playlistList.isEmpty()) {
                noPlLayout.setVisibility(View.VISIBLE);
            }
            playlistAdapter = new PlaylistAdapter(getContext(), playlistList);
            recyclerView.setAdapter(playlistAdapter);
        } else {
            noPlLayout.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        noPlLayout = null;
        playlistList = null;
        playlistAdapter = null;
        super.onDestroyView();
    }
}