package com.atomykcoder.atomykplay;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.function.FetchMusic;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.MusicMainAdapter;
import com.atomykcoder.atomykplay.function.SearchResultsFragment;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.atomykcoder.atomykplay.player.PlayerFragment;
import com.atomykcoder.atomykplay.services.MediaPlayerService;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;
import com.turingtechnologies.materialscrollbar.Indicator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //♥♥☻☻
    //all public variables are in this format "public_variable"
    //all public and private static final string variables are in this format "PUBLIC_PRIVATE_STATIC_FINAL_STRING"
    //all private variables are in this format "privateVariable"
    //♥♥☻☻

    public static final String BROADCAST_PLAY_NEW_MUSIC = "com.atomykcoder.atomykplay.PlayNewMusic";
    public static final String BROADCAST_PAUSE_PLAY_MUSIC = "com.atomykcoder.atomykplay.PausePlayMusic";
    public static final String BROADCAST_PLAY_NEXT_MUSIC = "com.atomykcoder.atomykplay.PlayNextMusic";
    public static final String BROADCAST_PLAY_PREVIOUS_MUSIC = "com.atomykcoder.atomykplay.PlayPreviousMusic";
    public static boolean service_bound = false;
    public static boolean is_granted = false;
    public static MediaPlayerService media_player_service;
    public static ServiceConnection service_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            media_player_service = binder.getService();
            service_bound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service_bound = false;
            media_player_service = null;
        }
    };
    public SlidingUpPanelLayout sliding_up_panel_layout;
    private ArrayList<MusicDataCapsule> dataList;
    private MusicMainAdapter adapter;
    private LinearLayout linearLayout;
    private RecyclerView recyclerView;
    private SearchResultsFragment searchResultsFragment; // This being here is very important for search method to work
    private AudioManager audioManager;
    private DragScrollBar scrollBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //initializations
        linearLayout = findViewById(R.id.song_not_found_layout);
        recyclerView = findViewById(R.id.music_recycler);
        sliding_up_panel_layout = findViewById(R.id.sliding_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);


        searchResultsFragment = new SearchResultsFragment();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        dataList = new ArrayList<>();
        scrollBar = findViewById(R.id.dragScrollBar);

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);


        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        manager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(manager);

        scrollBar.setIndicator(new AlphabetIndicator(MainActivity.this),false);

        //Checking permissions before activity creation (method somewhere down in the script)
        checkPermission();

        sliding_up_panel_layout.setPanelSlideListener(onSlideChange());
        setFragmentInSlider();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("serviceState", service_bound);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        service_bound = savedInstanceState.getBoolean("serviceState");
    }

    @Override
    protected void onStart() {
        if (is_granted) {
            if (!service_bound) {
                Intent playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
                bindService(playerIntent, service_connection, Context.BIND_IMPORTANT);

                //this will start playing song as soon as it app starts if its connected to headset
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (audioManager.isWiredHeadsetOn()){
//                        playAudio();
//                    }
//                }
            }
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (service_bound) {
            //if media player is not playing it will stop the service
            if (media_player_service.media_player != null) {
                if (!media_player_service.media_player.isPlaying()) {
                    unbindService(service_connection);
                    stopService(new Intent(this, MediaPlayerService.class));
                    service_bound = false;
                }
            }
        }
    }

    public void playAudio() {
        //starting the service when permissions are granted
        //starting service if its not started yet otherwise it will send broadcast msg to service
        //we can't start service on startup of app it will lead to pausing all other sound playing on device
        new StorageUtil(this).clearMusicLastPos();

        if (!service_bound) {
            Intent playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, service_connection, Context.BIND_IMPORTANT);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //service is active send media with broadcast receiver
                    Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_MUSIC);
                    sendBroadcast(broadcastIntent);
                }
            }, 20);
        } else {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_MUSIC);
            sendBroadcast(broadcastIntent);
        }
    }

    private void setFragmentInSlider() {
        PlayerFragment fragment = new PlayerFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.commit();
    }

    private void setSearchFragment() {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.sec_container, searchResultsFragment);
        transaction.addToBackStack(searchResultsFragment.toString());
        transaction.commit();
    }


    //Checks whether user granted permissions for external storage or not
    //if not then shows dialogue to grant permissions
    private void checkPermission() {

        Dexter.withContext(MainActivity.this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        //Fetch Music List along with it's metadata and save it in "dataList"
                        FetchMusic.fetchMusic(dataList, MainActivity.this);

                        //Setting up adapter
                        linearLayout.setVisibility(View.GONE);
                        adapter = new MusicMainAdapter(MainActivity.this, dataList);
                        recyclerView.setAdapter(adapter);

                        is_granted = true;
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                        is_granted = false;
                    }
                }).check();

    }

    private SlidingUpPanelLayout.PanelSlideListener onSlideChange() {
        return new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                View miniPlayer = PlayerFragment.mini_play_view;
                View mainPlayer = findViewById(R.id.player_layout);
                miniPlayer.setAlpha(1 - slideOffset * 4);
                mainPlayer.setAlpha(0 + slideOffset * 2);
            }

            @Override
            public void onPanelCollapsed(View panel) {
                View miniPlayer = PlayerFragment.mini_play_view;
                View mainPlayer = findViewById(R.id.player_layout);
                miniPlayer.setVisibility(View.VISIBLE);
                miniPlayer.setAlpha(1);
                mainPlayer.setAlpha(0);
            }

            @Override
            public void onPanelExpanded(View panel) {
                //this will hide the keyboard after clicking on player while searching
                try {
                    InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    FragmentManager manager1 = getSupportFragmentManager();
                    manager1.popBackStack();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                View miniPlayer = PlayerFragment.mini_play_view;
                View mainPlayer = findViewById(R.id.player_layout);

                miniPlayer.setVisibility(View.INVISIBLE);
                miniPlayer.setAlpha(0);
                mainPlayer.setAlpha(1);
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
    public void onBackPressed() {
        if (sliding_up_panel_layout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            super.onBackPressed();
        } else {
            sliding_up_panel_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    //region Searchbar Functionality Code here
    // Adding SearchView Icon to Toolbar
    // Listening for Queries in Search View
    // Sending Queries to SearchResultFragment to process
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSearchFragment();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                SearchWithFilters(query);
                searchResultsFragment.search(query, dataList);
                return false;
            }

        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                FragmentManager manager = getSupportFragmentManager();
                manager.popBackStack();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    //runs Search Method if any radio button is pressed
    private void SearchWithFilters(String query) {

        //Check if any radio button is pressed
        searchResultsFragment.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                //search if any radio button is pressed
                searchResultsFragment.search(query, dataList);
            }
        });
    }
    //endregion


}