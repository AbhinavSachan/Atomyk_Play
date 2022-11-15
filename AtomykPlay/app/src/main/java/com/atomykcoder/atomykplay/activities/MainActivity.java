package com.atomykcoder.atomykplay.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.FoundLyricsAdapter;
import com.atomykcoder.atomykplay.adapters.MusicMainAdapter;
import com.atomykcoder.atomykplay.customScripts.CustomBottomSheet;
import com.atomykcoder.atomykplay.events.PrepareRunnableEvent;
import com.atomykcoder.atomykplay.fragments.BottomSheetPlayerFragment;
import com.atomykcoder.atomykplay.fragments.SearchResultsFragment;
import com.atomykcoder.atomykplay.function.FetchMusic;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.atomykcoder.atomykplay.services.MediaPlayerService;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    //♥♥☻☻
    //all public variables are in this format "public_variable"
    //all public and private static final string variables are in this format "PUBLIC_PRIVATE_STATIC_FINAL_STRING"
    //all private variables are in this format "privateVariable"
    //♥♥☻☻

    public static final String BROADCAST_PLAY_NEW_MUSIC = "com.atomykcoder.atomykplay.PlayNewMusic";
    public static final String BROADCAST_PAUSE_PLAY_MUSIC = "com.atomykcoder.atomykplay.PausePlayMusic";
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
    public static boolean phone_ringing = false;
    public static boolean is_playing = false;
    public View player_bottom_sheet;
    public CustomBottomSheet<View> mainPlayerSheetBehavior;
    public BottomSheetPlayerFragment bottomSheetPlayerFragment;
    public BottomSheetBehavior<View> lyricsListBehavior;
    private final SearchResultsFragment searchResultsFragment = new SearchResultsFragment();
    private View shadowMain;
    private View shadowLyrFound;

    private final BottomSheetBehavior.BottomSheetCallback lrcFoundCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                shadowLyrFound.setAlpha(0f);
            } else if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                shadowLyrFound.setAlpha(0.5f);
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                shadowLyrFound.setAlpha(1f);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            shadowLyrFound.setAlpha(0.5f + slideOffset);
        }
    };
    private ArrayList<MusicDataCapsule> dataList;
    private MusicMainAdapter adapter;
    private LinearLayout linearLayout;
    private RecyclerView recyclerView;
    private AudioManager audioManager;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private DrawerLayout drawer;
    private ProgressBar progressBar;
    private View lyricsListView;
    private FragmentManager searchFragmentManager;
    public BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                View miniPlayer = bottomSheetPlayerFragment.mini_play_view;
                View mainPlayer = findViewById(R.id.player_layout);
                mainPlayer.setVisibility(View.INVISIBLE);
                miniPlayer.setVisibility(View.VISIBLE);
                miniPlayer.setAlpha(1);
                shadowMain.setAlpha(0);
                mainPlayer.setAlpha(0);
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                //this will hide the keyboard after clicking on player while searching
                try {
                    InputMethodManager manager1 = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    manager1.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    searchFragmentManager.popBackStack();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                View miniPlayer = bottomSheetPlayerFragment.mini_play_view;
                View mainPlayer = findViewById(R.id.player_layout);
                miniPlayer.setVisibility(View.INVISIBLE);
                mainPlayer.setVisibility(View.VISIBLE);
                miniPlayer.setAlpha(0);
                mainPlayer.setAlpha(1);
                shadowMain.setAlpha(1);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            View miniPlayer = bottomSheetPlayerFragment.mini_play_view;
            View mainPlayer = findViewById(R.id.player_layout);
            miniPlayer.setVisibility(View.VISIBLE);
            mainPlayer.setVisibility(View.VISIBLE);
            miniPlayer.setAlpha(1 - slideOffset * 15);
            mainPlayer.setAlpha(0 + slideOffset * 3);
            shadowMain.setAlpha(0 + slideOffset);
        }
    };
    private FragmentManager mainPlayerManager;

    //endregion
    public static int convertToMillis(String duration) {
        int out;
        String _duration = duration.replace("[", "").replace("]", "");
        String[] numbers = _duration.split(":");
        int first = Integer.parseInt(numbers[0]);
        int second = Integer.parseInt(numbers[1]);
        first = first * (60 * 1000);
        second = second * 1000;
        out = first + second;
        return out;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        SharedPreferences sharedPreferencesSet = getSharedPreferences(getString(R.string.set_sp),Context.MODE_PRIVATE);
//        boolean switch1 = sharedPreferencesSet.getBoolean(SWITCH1, false);
//        if (switch1) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
//        }
        setContentView(R.layout.activity_main);

        //initializations
        mainPlayerManager = getSupportFragmentManager();
        searchFragmentManager = getSupportFragmentManager();

        linearLayout = findViewById(R.id.song_not_found_layout);
        recyclerView = findViewById(R.id.music_recycler);
        player_bottom_sheet = findViewById(R.id.player_main_container);
        progressBar = findViewById(R.id.progress_bar_main_activity);
        shadowMain = findViewById(R.id.shadow_main);
        shadowLyrFound = findViewById(R.id.shadow_lyrics_found);
        mainPlayerSheetBehavior = (CustomBottomSheet<View>) BottomSheetBehavior.from(player_bottom_sheet);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        CoordinatorLayout main_layout = findViewById(R.id.main_layout);

        MediaPlayerService.ui_visible = true;

        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        drawer = findViewById(R.id.drawer_layout);

        View headerView = navigationView.getHeaderView(0);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_toggle_open, R.string.drawer_toggle_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        lyricsListView = findViewById(R.id.found_lyrics_fragments);
        lyricsListBehavior = BottomSheetBehavior.from(lyricsListView);
        lyricsListBehavior.setHideable(true);
        lyricsListBehavior.setSkipCollapsed(true);
        lyricsListBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        main_layout.setNestedScrollingEnabled(false);


        dataList = new ArrayList<>();
        DragScrollBar scrollBar = findViewById(R.id.dragScrollBar);

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);


        scrollBar.setRecyclerView(recyclerView);
        scrollBar.setIndicator(new AlphabetIndicator(MainActivity.this), false);

        //Checking permissions before activity creation (method somewhere down in the script)
        checkPermission();
        if (is_granted) {
            callStateListener();
        }
        setFragmentInSlider();

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        player_bottom_sheet.setClickable(true);
//        bottom_sheet.setNestedScrollingEnabled(true);
        mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mainPlayerSheetBehavior.setPeekHeight(136);
        mainPlayerSheetBehavior.addBottomSheetCallback(callback);
        lyricsListBehavior.addBottomSheetCallback(lrcFoundCallback);

    }

    public void setLyricListAdapter(Bundle bundle) {
        ArrayList<String> titles;
        ArrayList<String> sampleLyrics;
        ArrayList<String> urls;

        titles = bundle.getStringArrayList("titles");
        sampleLyrics = bundle.getStringArrayList("sampleLyrics");
        urls = bundle.getStringArrayList("urls");

        RecyclerView recyclerView = findViewById(R.id.found_lyrics_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        FoundLyricsAdapter adapter = new FoundLyricsAdapter(titles, sampleLyrics, urls, this);
        recyclerView.setAdapter(adapter);
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
            bindService();
            Log.d("bound", "STARTED");
            //                this will start playing song as soon as app starts if its connected to headset

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (audioManager.isBluetoothScoOn()){
                        playAudio();
                    }else if (audioManager.isWiredHeadsetOn()){
                        playAudio();
                    }
                }
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (service_bound) {
            if (is_playing) {
                media_player_service.setSeekBar();
                EventBus.getDefault().post(new PrepareRunnableEvent("On Resume"));
            }
        }
    }

    @Override
    protected void onStop() {
        if (service_bound) {
            MainActivity.this.unbindService(service_connection);
            Log.d("bound", "STOPPED");
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (service_bound) {
            if (media_player_service.handler != null) {
                media_player_service.handler.removeCallbacks(media_player_service.runnable);
                if (BottomSheetPlayerFragment.lyricsHandler != null) {
                    BottomSheetPlayerFragment.lyricsHandler.removeCallbacks(BottomSheetPlayerFragment.lyricsRunnable);
                }
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
        if (service_bound) {
            //if media player is not playing it will stop the service
            if (media_player_service.media_player != null) {
                if (!is_playing) {
                    MainActivity.this.unbindService(service_connection);
                    stopService(new Intent(MainActivity.this, MediaPlayerService.class));
                    service_bound = false;
                    is_playing = false;
                }
            }
        }
        MediaPlayerService.ui_visible = false;
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

    }

    private void bindService() {
        if (!phone_ringing) {
            Intent playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
            MainActivity.this.bindService(playerIntent, service_connection, Context.BIND_AUTO_CREATE);
            startService(playerIntent);
        } else {
            Toast.makeText(this, "Can't play while on call.", Toast.LENGTH_SHORT).show();
        }
    }

    public void playAudio() {
        //starting the service when permissions are granted
        //starting service if its not started yet otherwise it will send broadcast msg to service
        //we can't start service on startup of app it will lead to pausing all other sound playing on device
        new StorageUtil(this).clearMusicLastPos();

        if (!phone_ringing) {
            if (!service_bound) {
                bindService();
                new Handler().postDelayed(() -> {
                    //service is active send media with broadcast receiver
                    Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_MUSIC);
                    sendBroadcast(broadcastIntent);
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

    public void pausePlayAudio() {
        if (!service_bound) {
            bindService();
            new Handler().postDelayed(() -> {
                //service is active send media with broadcast receiver
                Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
                sendBroadcast(broadcastIntent);
            }, 0);
        } else {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
            sendBroadcast(broadcastIntent);
        }
    }

    public void playNextAudio() {
        if (!service_bound) {
            bindService();
            new Handler().postDelayed(() -> {
                //service is active send media with broadcast receiver
                Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEXT_MUSIC);
                sendBroadcast(broadcastIntent);
            }, 0);
        } else {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEXT_MUSIC);
            sendBroadcast(broadcastIntent);
        }
    }

    public void playPreviousAudio() {
        if (!service_bound) {
            bindService();
            new Handler().postDelayed(() -> {
                //service is active send media with broadcast receiver
                Intent broadcastIntent = new Intent(BROADCAST_PLAY_PREVIOUS_MUSIC);
                sendBroadcast(broadcastIntent);
            }, 0);

        } else {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_PREVIOUS_MUSIC);
            sendBroadcast(broadcastIntent);
        }
    }

    private void setFragmentInSlider() {
        bottomSheetPlayerFragment = new BottomSheetPlayerFragment();

        FragmentTransaction transaction = mainPlayerManager.beginTransaction();
        transaction.replace(R.id.player_main_container, bottomSheetPlayerFragment);
        transaction.commit();
    }

    private void setSearchFragment() {
        FragmentTransaction transaction = searchFragmentManager.beginTransaction();
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

                        ExecutorService service = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());
                        progressBar.setVisibility(View.VISIBLE);
                        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
                        manager.setSmoothScrollbarEnabled(true);


                        // do in background code here
                        service.execute(() -> {

                            FetchMusic.fetchMusic(dataList, MainActivity.this);
                            // post-execute code here
                            handler.post(() -> {
                                //saving this in storage for diff util
                                new StorageUtil(MainActivity.this).saveInitialMusicList(dataList);

                                //Setting up adapter
                                if (dataList.isEmpty()) {
                                    linearLayout.setVisibility(View.VISIBLE);
                                } else {
                                    linearLayout.setVisibility(View.GONE);
                                }
                                progressBar.setVisibility(View.GONE);
                                adapter = new MusicMainAdapter(MainActivity.this, dataList);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(manager);

                            });
                        });
                        service.shutdown();

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
        if (mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            super.onBackPressed();

        } else {
            if (BottomSheetPlayerFragment.queueSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                BottomSheetPlayerFragment.queueSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

        searchView.setOnSearchClickListener(view -> setSearchFragment());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                searchResultsFragment.SearchWithFilters(query, dataList);
                searchResultsFragment.search(query, dataList);
                return false;
            }

        });
        searchView.setOnCloseListener(() -> {
            searchFragmentManager.popBackStack();
            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }

    //runs Search Method if any radio button is pressed


}