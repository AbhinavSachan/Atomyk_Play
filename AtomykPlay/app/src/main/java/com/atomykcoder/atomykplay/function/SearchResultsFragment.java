package com.atomykcoder.atomykplay.function;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;

import java.util.ArrayList;

//Search Layout Fragment for Performing Searches and Presenting Results
public class SearchResultsFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public RecyclerView recycler_view;
    private ArrayList<MusicDataCapsule> originalMusicList;
    private MusicAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        recycler_view = view.findViewById(R.id.search_recycler_view);
        originalMusicList = new ArrayList<>();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setItemAnimator(new DefaultItemAnimator());

        setAdapter();

        return view;
    }

    //Initializing And setting an adapter
    private void setAdapter() {
        adapter = new MusicAdapter(getContext(), originalMusicList);
        recycler_view.setAdapter(adapter);
    }

    //Function that adds music to an arraylist which is being used to show music in recycler view
    private void addMusic(MusicDataCapsule song) {
        originalMusicList.add(song);
    }

    //Function that performs searches and if it finds a match we add that song to our arraylist
    //Function also do cleanup from previous search
    public void search(String query, ArrayList<MusicDataCapsule> dataList) {
        cleanUp();
        if (!query.isEmpty()) {
            for (MusicDataCapsule song : dataList) {
                //searching by name and artist
                if (song.getsName().toLowerCase().contains(query) || song.getsArtist().toLowerCase().contains(query)) {
                    addMusic(song);
                }
            }
        }
    }

    //Cleaning up any search results left from last search
    //Refreshing list
    @SuppressLint("NotifyDataSetChanged")
    private void cleanUp() {
        originalMusicList.clear();
        adapter.notifyDataSetChanged();
    }
}