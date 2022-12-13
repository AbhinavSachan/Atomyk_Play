package com.atomykcoder.atomykplay.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.OpenPlayListAdapter;
import com.atomykcoder.atomykplay.adapters.SimpleTouchCallback;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.events.RemoveFromPlaylistEvent;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.atomykcoder.atomykplay.viewModals.Playlist;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class OpenPlayListFragment extends Fragment implements OnDragStartListener {

    private ItemTouchHelper itemTouchHelper;
    private OpenPlayListAdapter openPlayListAdapter;
    private ArrayList<MusicDataCapsule> musicList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_open_play_list, container, false);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        Playlist playlist = (Playlist) (getArguments() != null ? getArguments().getSerializable("currentPlaylist") : null);

        RecyclerView recyclerView = view.findViewById(R.id.open_pl_music_recycler);
        View noPlLayout = view.findViewById(R.id.song_not_found_layout_opl);
        CollapsingToolbarLayout collapsingToolbarLayout = view.findViewById(R.id.collapse_toolbar_opl);
        ImageView imageView = view.findViewById(R.id.toolbar_cover_opl);
        Toolbar toolbar = view.findViewById(R.id.toolbar_opl);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());

        StorageUtil storageUtil = new StorageUtil(getContext());

        if(playlist != null) {
            ArrayList<String> idList = playlist.getMusicIDList();
            musicList = storageUtil.getItemListFromInitialList(idList);
        }

        if (playlist != null) {
            collapsingToolbarLayout.setTitle(playlist.getName());
            GlideBuilt.glide(getContext(), playlist.getCoverUri(), 0, imageView, 512);
        }

        if (musicList != null) {
            openPlayListAdapter = new OpenPlayListAdapter(getContext(), playlist.getName(), musicList, this);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(openPlayListAdapter);
            noPlLayout.setVisibility(View.GONE);
            if (musicList.isEmpty()) {
                noPlLayout.setVisibility(View.VISIBLE);
            }
            ItemTouchHelper.Callback callback = new SimpleTouchCallback(openPlayListAdapter);
            itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        } else {
            noPlLayout.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Subscribe
    public void removeMusicFromList(RemoveFromPlaylistEvent event) {
        openPlayListAdapter.removeItem(event.music);
    }

    @Override
    public void onDragStart(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}