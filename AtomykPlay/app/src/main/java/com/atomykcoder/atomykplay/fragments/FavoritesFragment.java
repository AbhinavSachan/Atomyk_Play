package com.atomykcoder.atomykplay.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.FavoriteListAdapter;
import com.atomykcoder.atomykplay.adapters.SimpleTouchCallback;
import com.atomykcoder.atomykplay.customScripts.LinearLayoutManagerWrapper;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.events.RemoveFromFavoriteEvent;
import com.atomykcoder.atomykplay.utils.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment implements OnDragStartListener {

    private ItemTouchHelper itemTouchHelper;
    private FavoriteListAdapter playListAdapter;
    private StorageUtil storageUtil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        storageUtil = new StorageUtil(requireContext());

        RecyclerView recyclerView = view.findViewById(R.id.favorite_music_recycler);
        View noPlLayout = view.findViewById(R.id.song_not_found_layout_favorite);
        Toolbar toolbar = view.findViewById(R.id.toolbar_favorites);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager manager = new LinearLayoutManagerWrapper(getContext());

        ArrayList<Music> favList = storageUtil.getFavouriteList();


        if (favList != null) {
            playListAdapter = new FavoriteListAdapter(requireContext(), favList, this);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(playListAdapter);
            noPlLayout.setVisibility(View.GONE);

            if (favList.isEmpty()) {
                noPlLayout.setVisibility(View.VISIBLE);
            }

            ItemTouchHelper.Callback callback = new SimpleTouchCallback(playListAdapter);
            itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        } else {
            noPlLayout.setVisibility(View.VISIBLE);

        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDragStart(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Subscribe
    public void removeFromPlaylist(RemoveFromFavoriteEvent event) {
        playListAdapter.removeItem(event.music);
    }
}