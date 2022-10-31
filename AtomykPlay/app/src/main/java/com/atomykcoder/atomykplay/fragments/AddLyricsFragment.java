package com.atomykcoder.atomykplay.fragments;

import static com.atomykcoder.atomykplay.fragments.PlayerFragment.showToast;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.function.FetchLyrics;
import com.atomykcoder.atomykplay.function.FetchMusic;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.StorageUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddLyricsFragment extends Fragment {

    private String songName;
    private EditText editTextLyrics;
    private ProgressBar progressBar;
    private String artistName;
    private EditText nameEditText, artistEditText;
    private final Map<String, String> timestamps= new HashMap<>();

    private StorageUtil storageUtil;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_lyrics, container, false);

        nameEditText = view.findViewById(R.id.edit_song_name);
        artistEditText = view.findViewById(R.id.edit_artist_name);
        editTextLyrics = view.findViewById(R.id.edit_lyrics);
        Button saveBtn = view.findViewById(R.id.btn_save);
        Button btnFind = view.findViewById(R.id.btn_find);
        progressBar = view.findViewById(R.id.progress_lyrics);
        storageUtil = new StorageUtil(getContext());

        nameEditText.setText(getMusic().getsName());
        artistEditText.setText(getMusic().getsArtist());

        saveBtn.setOnClickListener(v -> showToast("saved☻"));

        btnFind.setOnClickListener(v -> invalidateEntry());
        invalidateEntry();
        return view;
    }

    private void updateLyrics() {
        int progress = PlayerFragment.getCurrentPos();
        String time = "[" + FetchMusic.convertDuration(String.valueOf(progress)) + "]";
        if (timestamps.containsKey(time)) {
            Log.i("KEY", timestamps.get(time));
        }
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
            fetchLyrics();
        }

    }

    private void fetchLyrics() {
        //show lyrics in bottom sheet
        artistName = artistEditText.getText().toString().toLowerCase().trim();
        songName = nameEditText.getText().toString().toLowerCase().trim();
        showToast("lyrics");
        timestamps.clear();
        FetchLyrics fetchLyrics = new FetchLyrics();
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            fetchLyrics.onPreExecute(progressBar);
            service.execute(() -> {
                String lyrics = fetchLyrics.fetch(artistName + " " + songName);
                handler.post(() -> {
                    fetchLyrics.onPostExecute(progressBar);
                    String strippedLyrics = stripLyrics(lyrics);
                    storeLyrics(lyrics, strippedLyrics);
                    editTextLyrics.setText(strippedLyrics);
                    for(Map.Entry<String, String> entry : timestamps.entrySet()) {
                        Log.i("SONG", "KEY: " + entry.getKey() + "  Value : " + entry.getValue());
                    }
                });
            });
            service.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeLyrics(String lyrics, String strippedLyrics) {
        Pattern p = Pattern.compile("\\[\\d\\d:\\d\\d");
        Matcher m = p.matcher(lyrics);
        String[] lines = strippedLyrics.split("\n");
        int index = 0;
        while(m.find() && index < lines.length) {
            String key = m.group() + "]";
            timestamps.put(key, lines[index]);
            index++;
        }
    }

    private String stripLyrics(String lyrics) {
        Pattern p = Pattern.compile("\\[(.*?)]");
        String result = lyrics.replaceAll(p.pattern(), "\n");
        result = result.trim();
        return result;
    }
}