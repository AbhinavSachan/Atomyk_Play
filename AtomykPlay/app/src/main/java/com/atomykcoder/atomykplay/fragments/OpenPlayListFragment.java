package com.atomykcoder.atomykplay.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.OpenPlayListAdapter;
import com.atomykcoder.atomykplay.adapters.SimpleTouchCallback;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.atomykcoder.atomykplay.viewModals.Playlist;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;


public class OpenPlayListFragment extends Fragment implements OnDragStartListener {

    private ItemTouchHelper itemTouchHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_open_play_list, container, false);

        Playlist playlist = (Playlist) (getArguments() != null ? getArguments().getSerializable("currentPlaylist") : null);

        RecyclerView recyclerView = view.findViewById(R.id.open_pl_music_recycler);
        View noPlLayout = view.findViewById(R.id.song_not_found_layout_opl);
        CollapsingToolbarLayout collapsingToolbarLayout = view.findViewById(R.id.collapse_toolbar_opl);
        ImageView imageView = view.findViewById(R.id.toolbar_cover_opl);
        Toolbar toolbar = view.findViewById(R.id.toolbar_opl);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());

        ArrayList<MusicDataCapsule> dataList = playlist != null ? playlist.getMusicArrayList() : null;

        if (playlist != null) {
            collapsingToolbarLayout.setTitle(playlist.getName());
            GlideBuilt.glide(getContext(),playlist.getCoverUri(),0,imageView,512);
        }

        if (dataList != null) {
            OpenPlayListAdapter playListAdapter = new OpenPlayListAdapter(getContext(), playlist.getName(), dataList, this);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(playListAdapter);
            noPlLayout.setVisibility(View.GONE);
            if (dataList.isEmpty()) {
                noPlLayout.setVisibility(View.VISIBLE);
            }
            ItemTouchHelper.Callback callback = new SimpleTouchCallback(playListAdapter);
            itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }

        return view;
    }

    @Override
    public void onDragStart(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}