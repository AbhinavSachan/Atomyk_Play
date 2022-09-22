package com.atomykcoder.atomykplay.function;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.atomykcoder.atomykplay.R;

import java.util.ArrayList;
import java.util.Collection;
//Search Layout Fragment for Performing Searches and Presenting Results
public class SearchResultsFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public RecyclerView recyclerView;
    private ArrayList<MusicDataCapsule> originalMusicList;
    private MusicAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_search_results, container, false);
        recyclerView = view.findViewById(R.id.search_recycler_view);
        originalMusicList = new ArrayList<>();

        setAdapter(view);

        return view;
    }

    //Initializing And setting an adapter
    private void setAdapter(View view) {
        adapter = new MusicAdapter(getContext(), originalMusicList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    //Function that adds music to an arraylist which is being used to show music in recycler view
    private void addMusic(MusicDataCapsule song){
        originalMusicList.add(song);
    }

    //Function that performs searches and if it finds a match we add that song to our arraylist
    //Function also do cleanup from previous search
    public void search(String query, ArrayList<MusicDataCapsule> dataList) {
        cleanUp();
        if(!query.isEmpty()) {
            for (MusicDataCapsule song : dataList) {
                if (song.getsName().toLowerCase().contains(query)) {
                    addMusic(song);
                }
            }
            }
    }
    //Cleaning up any search results left from last search
    //Refreshing list
    private void cleanUp(){
        originalMusicList.clear();
        adapter.notifyDataSetChanged();
    }
}