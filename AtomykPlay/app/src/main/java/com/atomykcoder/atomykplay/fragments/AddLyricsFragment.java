package com.atomykcoder.atomykplay.fragments;

import static com.atomykcoder.atomykplay.fragments.PlayerFragment.showToast;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.atomykcoder.atomykplay.function.LRCMap;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.StorageUtil;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddLyricsFragment extends Fragment {

    private final LRCMap lrcMap = new LRCMap();
    private String songName;
    private EditText editTextLyrics;
    private ProgressBar progressBar;
    private String artistName;
    private EditText nameEditText, artistEditText;
    private StorageUtil storageUtil;
    private Button saveBtn, btnFind;
    private Dialog dialog;
    private Button btnCancel, btnOk;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_lyrics, container, false);

        editTextLyrics = view.findViewById(R.id.edit_lyrics);
        saveBtn = view.findViewById(R.id.btn_save);
        btnFind = view.findViewById(R.id.btn_find);
        progressBar = view.findViewById(R.id.progress_lyrics);
        storageUtil = new StorageUtil(getContext());

        TextView nameText = view.findViewById(R.id.song_name_tv);
        nameText.setText(getMusic().getsName());

        saveBtn.setOnClickListener(v -> saveMusic());
        btnFind.setOnClickListener(v -> {
            setDialogBox();
        });

        return view;
    }

    private void saveMusic() {
        if (lrcMap.isEmpty()) {
            showToast("Nothing to save");
        } else {
            if (storageUtil.loadLyrics(getMusic().getsName()) == null)
                storageUtil.saveLyrics(getMusic().getsName(), lrcMap);
            else {
                storageUtil.removeLyrics(getMusic().getsName());
                storageUtil.saveLyrics(getMusic().getsName(), lrcMap);
            }
            showToast("Saved");
        }
    }

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
            if (isValidInput())
                fetchLyrics();
        });
        btnCancel.setOnClickListener(v -> {
            dialog.cancel();
        });
        dialog.show();
    }

    private boolean isValidInput() {
        artistName = artistEditText.getText().toString().toLowerCase().trim();
        songName = nameEditText.getText().toString().toLowerCase().trim();

        if (artistName.equals("")) {
            artistEditText.setError("Required");
            return false;
        } else if (songName.equals("")) {
            nameEditText.setError("Required");
            return false;
        } else {
            dialog.cancel();
            return true;
        }

    }

    private void fetchLyrics() {
        //show lyrics in bottom sheet
        artistName = artistEditText.getText().toString().toLowerCase().trim();
        songName = nameEditText.getText().toString().toLowerCase().trim();

        //clear hashmap prior to retrieving data
        lrcMap.clear();

        showToast("fetching");

        FetchLyrics fetchLyrics = new FetchLyrics();
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            // pre-execute some code here
            fetchLyrics.onPreExecute(progressBar);

            // do in background code here
            service.execute(() -> {
                String _unfilteredLyrics = fetchLyrics.fetch(artistName + " " + songName);

                // post-execute code here
                handler.post(() -> {
                    fetchLyrics.onPostExecute(progressBar);

                    String _filteredLyrics = splitLyricsByNewLine(_unfilteredLyrics);
                    editTextLyrics.setText(_filteredLyrics);
                    lrcMap.addAll(getLrcMap(_filteredLyrics));

                });
            });

            // stopping the background thread (crucial)
            service.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * This function takes unfiltered lrc data and returns a linked hashmap with timestamp as
     * keys and their assigned lyrics as values.
     *
     * @param lyrics unfiltered lrc data retrieved from megalobiz
     * @return linked hashmap with timestamp as keys and their designated lyrics as values
     */
    private LRCMap getLrcMap(String lyrics) {
        LRCMap _lrcMap = new LRCMap();
        Pattern _pattern = Pattern.compile("\\[\\d\\d:\\d\\d");
        Matcher _timestamps = _pattern.matcher(lyrics);
        String _filteredLyrics = filter(lyrics);
        String[] lines = _filteredLyrics.split("\n");
        int i = 0;
        while (_timestamps.find() && i < lines.length) {
            _lrcMap.add(_timestamps.group() + "]", lines[i]);
            i++;
        }
        return _lrcMap;
    }

    private String splitLyricsByNewLine(String lyrics) {
        Pattern p = Pattern.compile("\\[");
        String result = lyrics.replaceAll(p.pattern(), "\n\\[");
        result = result.trim();
        return result;
    }


    /**
     * filter() takes a string argument lyrics. It removes any un-needed information and returns
     * filtered string lyrics which only contains characters a-z, A-z, 0-9
     *
     * @param lyrics that need to be filtered.
     * @return filtered string lyrics.
     */
    private String filter(String lyrics) {
        Pattern p = Pattern.compile("\\[(.*?)]");
        String result = lyrics.replaceAll(p.pattern(), "");
        result = result.trim();
        return result;
    }

    /**
     * retrieve current music loaded into player fragment
     *
     * @return active music
     */
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
}