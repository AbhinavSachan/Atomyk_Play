package com.atomykcoder.atomykplay.fragments;

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
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.events.RunnableSyncLyricsEvent;
import com.atomykcoder.atomykplay.helperFunctions.FetchLyrics;
import com.atomykcoder.atomykplay.viewModals.LRCMap;
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddLyricsFragment extends Fragment {

    private final LRCMap lrcMap = new LRCMap();
    private String songName;
    private EditText editTextLyrics;
    private ProgressBar progressBar;
    private String artistName;
    private EditText nameEditText, artistEditText;
    private StorageUtil storageUtil;
    private Button btnFind;
    private Dialog dialog;
    private String name, artist,musicId;
    private View view;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        view = null;
        btnFind = null;
        editTextLyrics = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_lyrics, container, false);

        MusicDataCapsule selectedMusic = (MusicDataCapsule) (getArguments() != null ? getArguments().getSerializable("selectedMusic") : null);

        editTextLyrics = view.findViewById(R.id.edit_lyrics);
        Button saveBtn = view.findViewById(R.id.btn_save);
        btnFind = view.findViewById(R.id.btn_find);
        progressBar = view.findViewById(R.id.progress_lyrics);
        storageUtil = new StorageUtil(getContext());

        Toolbar toolbar = view.findViewById(R.id.toolbar_add_lyric);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        name = selectedMusic != null ? selectedMusic.getsName() : "";
        artist = selectedMusic != null ? selectedMusic.getsArtist() : "";
        musicId = selectedMusic != null ? selectedMusic.getsId() : "";

        TextView nameText = view.findViewById(R.id.song_name_tv);
        nameText.setText(name);

        saveBtn.setOnClickListener(v -> saveMusic());
        btnFind.setOnClickListener(v -> setDialogBox());

        return view;
    }


    private void saveMusic() {
        if (lrcMap.isEmpty()) {
            if (!editTextLyrics.getText().toString().trim().equals("")) {
                String lyricsSplitByNewLine = editTextLyrics.getText().toString();
                lrcMap.addAll(MusicHelper.getLrcMap(lyricsSplitByNewLine));
                saveLyrics();
            } else {
                showToast("Can't Save");
            }
        } else {
            saveLyrics();
        }

    }

    private void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }


    private void saveLyrics() {
        if (storageUtil.loadLyrics(musicId) == null)
            storageUtil.saveLyrics(musicId, lrcMap);
        else {
            storageUtil.removeLyrics(musicId);
            storageUtil.saveLyrics(musicId, lrcMap);
        }
        showToast("Saved");
        EventBus.getDefault().post(new RunnableSyncLyricsEvent());
    }

    private void setDialogBox() {
        dialog = new Dialog(getContext());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_name_dialog_box);

        //Initialize Dialogue Box UI Items
        nameEditText = dialog.findViewById(R.id.edit_song_name);
        artistEditText = dialog.findViewById(R.id.edit_artist_name);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnOk = dialog.findViewById(R.id.btn_ok);

        nameEditText.setText(name);
        artistEditText.setText(artist);

        btnOk.setOnClickListener(v -> {
            if (isValidInput()) {
                btnFind.setVisibility(View.GONE);
                fetchLyrics();
            }
        });
        btnCancel.setOnClickListener(v -> dialog.cancel());
        dialog.show();
    }

    private boolean isValidInput() {
        artistName = artistEditText.getText().toString().toLowerCase().trim();
        songName = nameEditText.getText().toString().toLowerCase().trim();

        if (songName.equals("")) {
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

        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        FetchLyrics fetchLyrics = new FetchLyrics();
        try {
            // pre-execute some code here
            fetchLyrics.onPreExecute(progressBar);

            // do in background code here
            service.execute(() -> {
                Bundle lyricsItems = null;
                try {
                    lyricsItems = fetchLyrics.fetchList(songName + " " + artistName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Bundle finalLyricsItems = lyricsItems;
                handler.post(() -> {
                    if (view != null) {
                        if (finalLyricsItems != null) {
                            ((MainActivity) requireContext()).openBottomSheet(finalLyricsItems);
                        } else {
                            Toast.makeText(requireContext(), "No Lyrics Found", Toast.LENGTH_SHORT).show();
                        }
                        fetchLyrics.onPostExecute(progressBar);
                        btnFind.setVisibility(View.VISIBLE);
                    }
                });
            });

            // stopping the background thread (crucial)
            service.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void loadSelectedLyrics(String href) {
        FetchLyrics fetchLyrics = new FetchLyrics();
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            // pre-execute some code here
            fetchLyrics.onPreExecute(progressBar);
            btnFind.setVisibility(View.INVISIBLE);

            // do in background code here
            service.execute(() -> {
                String unfilteredLyrics = fetchLyrics.fetchTimeStamps(href);
                handler.post(() -> {
                    fetchLyrics.onPostExecute(progressBar);
                    btnFind.setVisibility(View.VISIBLE);

                    String filteredLyrics = MusicHelper.splitLyricsByNewLine(unfilteredLyrics);
                    editTextLyrics.setText(filteredLyrics);
                    lrcMap.addAll(MusicHelper.getLrcMap(filteredLyrics));
                });
            });
            // stopping the background thread (crucial)
            service.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}