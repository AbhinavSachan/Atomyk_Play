package com.atomykcoder.atomykplay.fragments;

import static com.atomykcoder.atomykplay.fragments.PlayerFragment.showToast;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.function.FetchLyrics;
import com.atomykcoder.atomykplay.function.FetchMusic;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.StorageUtil;
import java.util.ArrayList;
import java.util.Arrays;
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
<<<<<<< Updated upstream
    public final Map<String, String> timestamps= new HashMap<>();
    public final ArrayList<String> lyrics = new ArrayList<>();

    private StorageUtil storageUtil;
=======
    private Button saveBtn, btnFind;
    private StorageUtil storageUtil;
    private Button btnCancel, btnOk;

    public static void setLyrics(String lyrics) {
        if (!lyrics.equals("") && lyrics.contains(songName)) {
            progressBar.setVisibility(View.GONE);
            lyricsEditText.setText(lyrics);
        }else {
            progressBar.setVisibility(View.GONE);
            showToast("Lyrics not found");
        }
    }

>>>>>>> Stashed changes
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_lyrics, container, false);

<<<<<<< Updated upstream
        nameEditText = view.findViewById(R.id.edit_song_name);
        artistEditText = view.findViewById(R.id.edit_artist_name);
        editTextLyrics = view.findViewById(R.id.edit_lyrics);
        Button saveBtn = view.findViewById(R.id.btn_save);
        Button btnFind = view.findViewById(R.id.btn_find);
=======
        lyricsEditText = view.findViewById(R.id.edit_lyrics);
        saveBtn = view.findViewById(R.id.btn_save);
        btnFind = view.findViewById(R.id.btn_find);
>>>>>>> Stashed changes
        progressBar = view.findViewById(R.id.progress_lyrics);
        storageUtil = new StorageUtil(getContext());

        TextView nameText = view.findViewById(R.id.song_name_tv);
        nameText.setText(getMusic().getsName());

        saveBtn.setOnClickListener(v -> showToast("saved☻"));
        btnFind.setOnClickListener(v -> invalidateEntry());

<<<<<<< Updated upstream
=======
        btnFind.setOnClickListener(v -> {
            setDialogBox();
        });
>>>>>>> Stashed changes

        return view;
    }
private Dialog dialog;
    private void setDialogBox() {
        dialog = new Dialog(getContext());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.edit_name_dialog_box);

        //Initialize Dialogue Box UI Items
        nameEditText = dialog.findViewById(R.id.edit_song_name);
        artistEditText = dialog.findViewById(R.id.edit_artist_name);
        btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnOk = dialog.findViewById(R.id.btn_ok);

        nameEditText.setText(getMusic().getsName());
        artistEditText.setText(getMusic().getsArtist());


        btnOk.setOnClickListener(v -> {
            invalidateEntry();
        });
        btnCancel.setOnClickListener(v->{
            dialog.cancel();
        });
        dialog.show();
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
<<<<<<< Updated upstream
=======
            dialog.cancel();
            progressBar.setVisibility(View.VISIBLE);
>>>>>>> Stashed changes
            fetchLyrics();
        }

    }

    private void fetchLyrics() {
        //show lyrics in bottom sheet
        artistName = artistEditText.getText().toString().toLowerCase().trim();
        songName = nameEditText.getText().toString().toLowerCase().trim();
<<<<<<< Updated upstream
        showToast("lyrics");
        timestamps.clear();
=======

        showToast("fetching");
>>>>>>> Stashed changes
        FetchLyrics fetchLyrics = new FetchLyrics();
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            fetchLyrics.onPreExecute(progressBar);
            service.execute(() -> {
                String rawLyrics = fetchLyrics.fetch(artistName + " " + songName);
                handler.post(() -> {
                    fetchLyrics.onPostExecute(progressBar);
                    String fixedLyrics = splitLyrics(rawLyrics);
                    editTextLyrics.setText(fixedLyrics);
                    storeLyrics(rawLyrics);
                    for(Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                        Log.i("TIMESTAMP", "KEY : " + timestamp.getKey() + "  VALUE : " + timestamp.getValue());
                    }
//                    ArrayList<String> lyrics = new ArrayList<>();
                });
            });
            service.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeLyrics(String lyrics) {
        Pattern p = Pattern.compile("\\[\\d\\d:\\d\\d");
        Matcher m = p.matcher(lyrics);
        String strippedLyrics = stripLyrics(lyrics);
        String[] lines = strippedLyrics.split("\n");
        int index = 0;
        while(m.find() && index < lines.length) {
            String key = m.group() + "]";
            timestamps.put(key, lines[index]);
            index++;
        }
    }

    private String splitLyrics(String lyrics) {
        Pattern p = Pattern.compile("\\[");
        String result = lyrics.replaceAll(p.pattern(), "\n\\[");
        result = result.trim();
        return result;
    }

    private String stripLyrics(String lyrics) {
        Pattern p = Pattern.compile("\\[(.*?)]");
        String result = lyrics.replaceAll(p.pattern(), "\n");
        result = result.trim();
        return result;
    }

}