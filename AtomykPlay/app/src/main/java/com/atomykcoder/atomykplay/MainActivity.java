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
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.fragments.CustomBottomSheet;
import com.atomykcoder.atomykplay.fragments.PlayerFragment;
import com.atomykcoder.atomykplay.function.FetchLyrics;
import com.atomykcoder.atomykplay.function.FetchMusic;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.MusicMainAdapter;
import com.atomykcoder.atomykplay.function.SearchResultsFragment;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.atomykcoder.atomykplay.services.MediaPlayerService;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;

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
    public static final String MEDIA_BUTTON_RECEIVER = "com.atomykcoder.atomykplay.MEDIA_BUTTON_RECEIVER";
    public static final String BROADCAST_STOP_MUSIC = "com.atomykcoder.atomykplay.StopMusic";
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
    public View bottom_sheet;
    public boolean phone_ringing = false;
    public CustomBottomSheet<View> bottomSheetBehavior;
    private ArrayList<MusicDataCapsule> dataList;
    private MusicMainAdapter adapter;
    private LinearLayout linearLayout;
    private RecyclerView recyclerView;
    private SearchResultsFragment searchResultsFragment; // This being here is very important for search method to work
    private AudioManager audioManager;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private CoordinatorLayout main_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializations
        linearLayout = findViewById(R.id.song_not_found_layout);
        recyclerView = findViewById(R.id.music_recycler);
        bottom_sheet = findViewById(R.id.main_container);
        bottomSheetBehavior = (CustomBottomSheet<View>) BottomSheetBehavior.from(bottom_sheet);
        Toolbar toolbar = findViewById(R.id.toolbar);
        main_layout = findViewById(R.id.main_layout);

        main_layout.setNestedScrollingEnabled(true);

        searchResultsFragment = new SearchResultsFragment();

        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        dataList = new ArrayList<>();
        DragScrollBar scrollBar = findViewById(R.id.dragScrollBar);

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);


        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        manager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(manager);

        scrollBar.setIndicator(new AlphabetIndicator(MainActivity.this), false);

        //Checking permissions before activity creation (method somewhere down in the script)
        checkPermission();
        callStateListener();
        setFragmentInSlider();

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        bottom_sheet.setClickable(true);
//        bottom_sheet.setNestedScrollingEnabled(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(136);
        bottomSheetBehavior.addBottomSheetCallback(callback);


        // Fetch Google lol


    }

    public BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                View miniPlayer = PlayerFragment.mini_play_view;
                View mainPlayer = findViewById(R.id.player_layout);
                mainPlayer.setVisibility(View.INVISIBLE);
                miniPlayer.setVisibility(View.VISIBLE);
                miniPlayer.setAlpha(1);
                mainPlayer.setAlpha(0);
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
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
                mainPlayer.setVisibility(View.VISIBLE);
                miniPlayer.setAlpha(0);
                mainPlayer.setAlpha(1);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            View miniPlayer = PlayerFragment.mini_play_view;
            View mainPlayer = findViewById(R.id.player_layout);
            miniPlayer.setVisibility(View.VISIBLE);
            mainPlayer.setVisibility(View.VISIBLE);
            miniPlayer.setAlpha(1 - slideOffset * 15);
            mainPlayer.setAlpha(0 + slideOffset * 3);
        }
    };

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

//                this will start playing song as soon as app starts if its connected to headset

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (audioManager.isBluetoothScoOn()){
//                        playAudio();audioManager.startBluetoothSco();
//                    }else if (audioManager.isWiredHeadsetOn()){
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
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

    }

    public void playAudio() {
        //starting the service when permissions are granted
        //starting service if its not started yet otherwise it will send broadcast msg to service
        //we can't start service on startup of app it will lead to pausing all other sound playing on device
        new StorageUtil(this).clearMusicLastPos();

        if (!phone_ringing) {
            if (!service_bound) {
                Intent playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
                bindService(playerIntent, service_connection, Context.BIND_AUTO_CREATE);
                startService(playerIntent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //service is active send media with broadcast receiver
                        Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_MUSIC);
                        sendBroadcast(broadcastIntent);
                    }
                }, 0);
            } else {
                //service is active send media with broadcast receiver
                Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_MUSIC);
                sendBroadcast(broadcastIntent);
            }
        } else {
            Toast.makeText(this, "Can't play while on call", Toast.LENGTH_SHORT).show();
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

    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                super.onCallStateChanged(state, phoneNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING: {
                        phone_ringing = true;
                    }
                    break;
                    case TelephonyManager.CALL_STATE_IDLE: {
                        phone_ringing = false;
                    }
                    break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
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
                        //saving this in storage for diff util
                        new StorageUtil(MainActivity.this).saveInitialMusicList(dataList);

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

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            super.onBackPressed();

        } else {
            if (PlayerFragment.queueSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                PlayerFragment.queueSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
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