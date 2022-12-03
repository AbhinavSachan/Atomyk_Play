package com.atomykcoder.atomykplay.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.FavoriteListAdapter;
import com.atomykcoder.atomykplay.adapters.OpenPlayListAdapter;
import com.atomykcoder.atomykplay.adapters.SimpleTouchCallback;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment implements OnDragStartListener {

    private ItemTouchHelper itemTouchHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_favorites, container, false);

        StorageUtil storageUtil =new StorageUtil(getContext());

        RecyclerView recyclerView = view.findViewById(R.id.favorite_music_recycler);
        View noPlLayout = view.findViewById(R.id.song_not_found_layout_favorite);
        Toolbar toolbar = view.findViewById(R.id.toolbar_favorites);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());

        ArrayList<MusicDataCapsule> dataList = storageUtil.getFavouriteList();


        if (dataList != null) {
            FavoriteListAdapter playListAdapter = new FavoriteListAdapter(getContext(), dataList, this);
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