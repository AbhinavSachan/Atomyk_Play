package com.atomykcoder.atomykplay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.musicload.MusicAdapter;
import com.atomykcoder.atomykplay.musicload.MusicDataCapsule;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<MusicDataCapsule> dataList;
    private MusicAdapter adapter;
    private LinearLayout linearLayout;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //initializations
        linearLayout = findViewById(R.id.song_not_found_layout);
        recyclerView = findViewById(R.id.music_recycler);
        dataList = new ArrayList<>();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));


        //Checks permissions (method somewhere down in the script)
        checkPermission();
    }

    //Checks whether user granted permissions for external storage or not
    //if not then shows dialogue to grant permissions
    void checkPermission() {

        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        //Fetch Music List along with it's metadata and save it in "dataList"
                        fetchMusic(dataList);

                        //Setting up adapter
                        linearLayout.setVisibility(View.GONE);
                        adapter = new MusicAdapter(MainActivity.this, dataList);
                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        linearLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    void fetchMusic(ArrayList<MusicDataCapsule> dataList) {
        //Creating an array for data types we need
        String[] proj = {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
        };


        //Creating a cursor to store all data of a song
        Cursor audioCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                proj,
                null,
                null,
                null
        );

        //If cursor is not null then storing data inside a data list.
        if (audioCursor != null) {
            if (audioCursor.moveToFirst()) {
                do {

                    String songLength = convertDuration(audioCursor.getString(2));

                    dataList.add(new MusicDataCapsule(audioCursor.getString(0),
                            audioCursor.getString(1), songLength
                    ));
                } while (audioCursor.moveToNext());
            }
        }

        //Closing Cursor to prevent memory leaks
        assert audioCursor != null;
        audioCursor.close();

    }

    //converting duration from millis to readable time
    @SuppressLint("DefaultLocale")
    private String convertDuration(String duration) {
        String out;
        int dur = Integer.parseInt(duration);

        int hours = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hours == 0) {
            out = String.format("%02d:%02d", mns, scs);
        } else {
            out = String.format("%02d:%02d:%02d", hours, mns, scs);
        }
        return out;
    }
}