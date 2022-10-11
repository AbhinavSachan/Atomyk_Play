package com.atomykcoder.atomykplay.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.MusicQueueAdapter;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.interfaces.SimpleTouchCallback;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class BottomSheetQueueLayoutFragment extends BottomSheetDialogFragment implements OnDragStartListener {

    private ItemTouchHelper itemTouchHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_fragment_queue_layout, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.queue_music_recycler);
        ArrayList<MusicDataCapsule> dataList;

        //Setting up adapter
        dataList = new StorageUtil(getContext()).loadMusicList();
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MusicQueueAdapter adapter = new MusicQueueAdapter(getActivity(), dataList, this);
        recyclerView.setAdapter(adapter);


        ItemTouchHelper.Callback callback = new SimpleTouchCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    @Override
    public void onDragStart(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}
