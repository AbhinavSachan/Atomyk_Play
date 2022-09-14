package com.atomykcoder.atomykplay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

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

    private final ArrayList<MusicDataCapsule> dataList = new ArrayList<>();
    private MusicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.music_recycler);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        //Checks permissions (method somewhere down in the script)
        checkPermission();

        //Fetch Music List along with it's metadata and save it in "datalist"
        fetchMusic(dataList);

        //Prints Song Details in LogCat
        for (int i = 0; i < dataList.size(); i++)
        {
            Log.i("TAG", dataList.get(i).getsName());
            Log.i("TAG", dataList.get(i).getsArtist());
            Log.i("TAG", dataList.get(i).getsLength());
        }

        //Setting up adapter
        adapter = new MusicAdapter();
        recyclerView.setAdapter(adapter);
    }

    //Checks whether user granted permissions for external storage or not
    //if not then shows dialogue to grant permissions
    void checkPermission(){

        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }
    void fetchMusic(ArrayList<MusicDataCapsule> dataList){
        //Creating an array for data types we need
        String[] proj = {
                MediaStore.Audio.Media.ALBUM_ID,
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
        if(audioCursor != null){
            if(audioCursor.moveToFirst()){
                do{
                    dataList.add(new MusicDataCapsule(audioCursor.getString(1),
                            audioCursor.getString(2),
                            audioCursor.getString(3)));
                }while(audioCursor.moveToNext());
            }
        }

        //Closing Cursor to prevent memory leaks
        assert audioCursor != null;
        audioCursor.close();

    }
}