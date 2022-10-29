package com.atomykcoder.atomykplay.fragments;

import static com.atomykcoder.atomykplay.fragments.PlayerFragment.showToast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.function.FetchLyrics;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.StorageUtil;

import java.util.ArrayList;

public class AddLyricsFragment extends Fragment {

    private static String songName;
    private static EditText lyricsEditText;
    private static ProgressBar progressBar;
    //    private static String fetchedLyrics;
    private String artistName;
    private EditText nameEditText, artistEditText;
    private Button saveBtn, btnFind;

    public static void setLyrics(String lyrics) {
        if (lyrics.contains(songName)) {
            progressBar.setVisibility(View.GONE);
            lyricsEditText.setText(lyrics);
        }
    }
private StorageUtil storageUtil;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_lyrics, container, false);

        nameEditText = view.findViewById(R.id.edit_song_name);
        artistEditText = view.findViewById(R.id.edit_artist_name);
        lyricsEditText = view.findViewById(R.id.edit_lyrics);
        saveBtn = view.findViewById(R.id.btn_save);
        btnFind = view.findViewById(R.id.btn_find);
        progressBar = view.findViewById(R.id.progress_lyrics);
        storageUtil = new StorageUtil(getContext());

        nameEditText.setText(getMusic().getsName());
        artistEditText.setText(getMusic().getsArtist());

        saveBtn.setOnClickListener(v -> {
            showToast("saved☻");
        });

        btnFind.setOnClickListener(v -> {
            invalidateEntry();
        });

        return view;
    }

    private MusicDataCapsule getMusic() {
        ArrayList<MusicDataCapsule> musicList = storageUtil.loadMusicList();
        MusicDataCapsule activeMusic = null;
        int musicIndex;
        musicIndex = storageUtil.loadMusicIndex();

        if (musicList != null)
            if (musicIndex != -1 && musicIndex < musicList.size()) {
                activeMusic = musicList.get(musicIndex);
            } else {
                activeMusic = musicList.get(0);
            }
        return activeMusic;
    }

    private void invalidateEntry() {
        artistName = artistEditText.getText().toString().toLowerCase().trim();
        songName = nameEditText.getText().toString().toLowerCase().trim();

        if (artistName.equals("")) {
            artistEditText.setError("Required");
        } else if (songName.equals("")) {
            nameEditText.setError("Required");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            fetchLyrics();
        }

    }

    private void fetchLyrics() {
        //show lyrics in bottom sheet
        artistName = artistEditText.getText().toString().toLowerCase().trim();
        songName = nameEditText.getText().toString().toLowerCase().trim();

        showToast("lyrics");
        FetchLyrics fetchLyrics = new FetchLyrics();

        try {
            fetchLyrics.execute(artistName + " " + songName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    private String evalSongName(String song, String artist) {
//        song = song.toLowerCase();
//        artist = artist.toLowerCase();
//        if (song.contains("[")) {
//            song = song.substring(0, song.indexOf("["));
//        }
//        if (song.contains("(")) {
//            song = song.substring(0, song.indexOf("("));
//        }
//        if (song.contains("{")) {
//            song = song.substring(0, song.indexOf("{"));
//        }
//        if (song.contains(artist)) {
//            song = song.replace(artist, "");
//        }
//        if (song.contains("-")) {
//            song = song.replace("-", "");
//        }
//        if (song.contains("_")) {
//            song = song.replace("_", " ");
//        }
//        return song;
//    }
}