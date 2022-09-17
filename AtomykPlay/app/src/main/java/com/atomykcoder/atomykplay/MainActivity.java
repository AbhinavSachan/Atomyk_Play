package com.atomykcoder.atomykplay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.function.PlayerFragment;
import com.atomykcoder.atomykplay.musicload.FetchMusic;
import com.atomykcoder.atomykplay.musicload.MusicAdapter;
import com.atomykcoder.atomykplay.musicload.MusicDataCapsule;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public SlidingUpPanelLayout slidingUpPanelLayout;
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
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        dataList = new ArrayList<>();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        //Checks permissions (method somewhere down in the script)
        checkPermission();
        slidingUpPanelLayout.setPanelSlideListener(onSlideChange());
        setFragmentInSlider();
    }

    private SlidingUpPanelLayout.PanelSlideListener onSlideChange() {
        return new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                View miniPlayer = PlayerFragment.miniPlayView;
                miniPlayer.setAlpha(1 - slideOffset * 4);
            }

            @Override
            public void onPanelCollapsed(View panel) {
                View miniPlayer = PlayerFragment.miniPlayView;
                miniPlayer.setAlpha(1);
            }

            @Override
            public void onPanelExpanded(View panel) {
                View miniPlayer = PlayerFragment.miniPlayView;
                miniPlayer.setAlpha(0);
            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {

            }
        };
    }

    private void showToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
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
                        FetchMusic.fetchMusic(dataList,MainActivity.this);

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

    @Override
    public void onBackPressed() {
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            super.onBackPressed();
        } else {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    private void setFragmentInSlider() {
        PlayerFragment fragment = new PlayerFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.commit();
    }


}