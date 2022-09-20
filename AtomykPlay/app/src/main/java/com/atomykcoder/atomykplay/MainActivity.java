package com.atomykcoder.atomykplay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.function.FetchMusic;
import com.atomykcoder.atomykplay.function.MusicAdapter;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.atomykcoder.atomykplay.player.PlayerFragment;
import com.atomykcoder.atomykplay.services.MediaPlayerService;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String Broadcast_PLAY_NEW_MUSIC = "com.atomykcoder.atomykplay.PlayNewMusic";

    public SlidingUpPanelLayout slidingUpPanelLayout;
    boolean serviceBound = false;
    private ArrayList<MusicDataCapsule> dataList;
    private MusicAdapter adapter;
    private LinearLayout linearLayout;
    private RecyclerView recyclerView;
    private MediaPlayerService mediaPlayerService;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

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

    public void playAudio(int position) {
        //check is service active
        StorageUtil storage = new StorageUtil(MainActivity.this);
        if (!serviceBound) {
            //Store serializable music list to sharedPreference
            storage.storeMusicList(dataList);
            storage.storeMusicIndex(position);

            Intent playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //store new position
            storage.storeMusicIndex(position);
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_MUSIC);
            sendBroadcast(broadcastIntent);
        }
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("serviceState", serviceBound);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serviceBound) {
            unbindService(serviceConnection);
            mediaPlayerService.stopSelf();
        }
    }

    private void showToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }


    //Checks whether user granted permissions for external storage or not
    //if not then shows dialogue to grant permissions
    void checkPermission() {
        Dexter.withContext(getApplicationContext())
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
                .withListener(new MultiplePermissionsListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        //Fetch Music List along with it's metadata and save it in "dataList"

                        FetchMusic.fetchMusic(dataList, MainActivity.this);

                        //Setting up adapter
                        linearLayout.setVisibility(View.GONE);
                        adapter = new MusicAdapter(MainActivity.this, dataList);
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
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


    //region Searchbar Functionality Code here
    // Adding SearchView Icon to Toolbar
    // Listening for Queries in Search View
    // Manipulating musicList to find music
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    //endregion
}