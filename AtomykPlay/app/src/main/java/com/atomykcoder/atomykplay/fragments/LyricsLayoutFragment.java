package com.atomykcoder.atomykplay.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.arch.core.executor.TaskExecutor;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.MainActivity;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.function.MusicLyricsAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;

public class LyricsLayoutFragment extends Fragment {

    private RecyclerView recyclerView;
    private View noLyricsLayout;
    private ArrayList<String> arrayList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.lyrics_layout, container, false);

        for(int i = 0; i < 10; i++) {
            arrayList.add("this is a value of i :" + i);
        }

        TextView button = view.findViewById(R.id.btn_add_lyrics);
        recyclerView = view.findViewById(R.id.lyrics_recycler_view);
        noLyricsLayout = view.findViewById(R.id.no_lyrics_layout);

        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        manager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(manager);

        MusicLyricsAdapter adapter = new MusicLyricsAdapter(getContext(),arrayList);
        recyclerView.setAdapter(adapter);

        button.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getContext();
            mainActivity.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            AddLyricsFragment addLyricsFragment = new AddLyricsFragment();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.sec_container, addLyricsFragment);
            transaction.addToBackStack(addLyricsFragment.toString());
            transaction.commit();
        });

        return view;
    }
}
