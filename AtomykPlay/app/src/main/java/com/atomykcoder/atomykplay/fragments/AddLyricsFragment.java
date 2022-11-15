package com.atomykcoder.atomykplay.fragments;

import android.app.Dialog;
import android.media.metrics.Event;
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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.events.LoadSelectedItemEvent;
import com.atomykcoder.atomykplay.function.FetchLyrics;
import com.atomykcoder.atomykplay.function.LRCMap;
import com.atomykcoder.atomykplay.function.LyricsHelper;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddLyricsFragment extends Fragment {

    private LRCMap lrcMap = new LRCMap();
    private String songName;
    private EditText editTextLyrics;
    private ProgressBar progressBar;
    private String artistName;
    private EditText nameEditText, artistEditText;
    private StorageUtil storageUtil;
    private Button btnFind;
    private Dialog dialog;
    private View view;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lrcMap = null;
        view = null;
        editTextLyrics = null;
        progressBar = null;
        artistName = null;
        nameEditText = null;
        artistEditText = null;
        storageUtil = null;
        btnFind = null;
        dialog = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view = inflater.inflate(R.layout.fragment_add_lyrics, container, false);
        EventBus.getDefault().register(this);
        editTextLyrics = view.findViewById(R.id.edit_lyrics);
        Button saveBtn = view.findViewById(R.id.btn_save);
        btnFind = view.findViewById(R.id.btn_find);
        progressBar = view.findViewById(R.id.progress_lyrics);
        storageUtil = new StorageUtil(getContext());

        TextView nameText = view.findViewById(R.id.song_name_tv);
        nameText.setText(getMusic().getsName());

        saveBtn.setOnClickListener(v -> saveMusic());
        btnFind.setOnClickListener(v -> setDialogBox());


        return view;
    }


    private void saveMusic() {
        if (lrcMap.isEmpty()) {
            if (!editTextLyrics.getText().toString().trim().equals("")) {
                String unfilteredLyrics = editTextLyrics.getText().toString();

                lrcMap.addAll(LyricsHelper.getLrcMap(unfilteredLyrics));
                showLyrics();
            } else {
                showToast("Can't Save");
            }
        } else {
            showLyrics();
        }

    }

    private void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }


    private void showLyrics() {
        if (storageUtil.loadLyrics(getMusic().getsName()) == null)
            storageUtil.saveLyrics(getMusic().getsName(), lrcMap);
        else {
            storageUtil.removeLyrics(getMusic().getsName());
            storageUtil.saveLyrics(getMusic().getsName(), lrcMap);
        }
        showToast("Saved");
        MainActivity mainActivity = (MainActivity) getContext();
        if (mainActivity != null) {
            mainActivity.bottomSheetPlayerFragment.runnableSyncLyrics();
        }
    }

    private void setDialogBox() {
        dialog = new Dialog(getContext());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.edit_name_dialog_box);

        //Initialize Dialogue Box UI Items
        nameEditText = dialog.findViewById(R.id.edit_song_name);
        artistEditText = dialog.findViewById(R.id.edit_artist_name);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnOk = dialog.findViewById(R.id.btn_ok);

        nameEditText.setText(getMusic().getsName());
        artistEditText.setText(getMusic().getsArtist());

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

        FetchLyrics fetchLyrics = new FetchLyrics();
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            // pre-execute some code here
            fetchLyrics.onPreExecute(progressBar);

            // do in background code here
            service.execute(() -> {
                Bundle lyricsItems = fetchLyrics.fetchList(songName + " " + artistName);
                handler.post(() -> {
                    openBottomSheet(lyricsItems);
                    fetchLyrics.onPostExecute(progressBar);
                    btnFind.setVisibility(View.VISIBLE);
                });
            });

            // stopping the background thread (crucial)
            service.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openBottomSheet(Bundle lyricsItems) {
        MainActivity mainActivity = (MainActivity) getContext();
        if (mainActivity != null) {
            mainActivity.lyricsListBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            mainActivity.setLyricListAdapter(lyricsItems);
        }
    }

    @Subscribe
    public void loadSelectedLyrics(LoadSelectedItemEvent event) {
        String href = event.href;
        FetchLyrics fetchLyrics = new FetchLyrics();
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            // pre-execute some code here
            fetchLyrics.onPreExecute(progressBar);

            // do in background code here
            service.execute(() -> {
                String unfilteredLyrics = fetchLyrics.fetchTimeStamps(href);
                handler.post(() -> {
                    fetchLyrics.onPostExecute(progressBar);
                    if (unfilteredLyrics.equals("")) {
                        showToast("No Lyrics Found");
                    } else {
                        String filteredLyrics = LyricsHelper.splitLyricsByNewLine(unfilteredLyrics);
                        try {
                            editTextLyrics.setText(filteredLyrics);
                            lrcMap.addAll(LyricsHelper.getLrcMap(filteredLyrics));
                        } catch (Exception ignored) {
                        }
                    }
                });
            });
            // stopping the background thread (crucial)
            service.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
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