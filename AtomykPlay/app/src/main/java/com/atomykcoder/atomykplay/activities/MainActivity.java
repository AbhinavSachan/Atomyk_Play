package com.atomykcoder.atomykplay.activities;

import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.dark;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.no_dark;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.system_follow;
import static com.atomykcoder.atomykplay.services.MediaPlayerService.is_playing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.FoundLyricsAdapter;
import com.atomykcoder.atomykplay.adapters.MusicMainAdapter;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.customScripts.CustomBottomSheet;
import com.atomykcoder.atomykplay.events.PrepareRunnableEvent;
import com.atomykcoder.atomykplay.events.RemoveLyricsHandlerEvent;
import com.atomykcoder.atomykplay.events.SearchEvent;
import com.atomykcoder.atomykplay.fragments.AboutFragment;
import com.atomykcoder.atomykplay.fragments.BottomSheetPlayerFragment;
import com.atomykcoder.atomykplay.fragments.SearchResultsFragment;
import com.atomykcoder.atomykplay.fragments.SettingsFragment;
import com.atomykcoder.atomykplay.helperFunctions.FetchMusic;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String BROADCAST_PLAY_NEW_MUSIC = "com.atomykcoder.atomykplay.PlayNewMusic";
    public static final String BROADCAST_PAUSE_PLAY_MUSIC = "com.atomykcoder.atomykplay.PausePlayMusic";
    public static final String BROADCAST_STOP_MUSIC = "com.atomykcoder.atomykplay.StopMusic";
    public static final String BROADCAST_PLAY_NEXT_MUSIC = "com.atomykcoder.atomykplay.PlayNextMusic";
    public static final String BROADCAST_PLAY_PREVIOUS_MUSIC = "com.atomykcoder.atomykplay.PlayPreviousMusic";


    public static boolean service_bound = false;
    public static boolean is_granted = false;
    public static boolean phone_ringing = false;

    public static MediaPlayerService media_player_service;
    public ServiceConnection service_connection = new ServiceConnection() {
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
    public View player_bottom_sheet;
    public CustomBottomSheet<View> mainPlayerSheetBehavior;
    public BottomSheetPlayerFragment bottomSheetPlayerFragment;
    public BottomSheetBehavior<View> lyricsListBehavior;
    public CustomBottomSheet<View> optionSheetBehavior;
    FragmentManager searchFragmentManager;
    private View shadowMain;
    private View shadowLyrFound;
    private final BottomSheetBehavior.BottomSheetCallback lrcFoundCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                shadowLyrFound.setAlpha(0.2f);
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                shadowLyrFound.setAlpha(1f);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            shadowLyrFound.setAlpha(0.2f + slideOffset);
        }
    };
    private final BottomSheetBehavior.BottomSheetCallback optionCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                shadowLyrFound.setAlpha(0.7f);
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                shadowLyrFound.setAlpha(1f);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            shadowLyrFound.setAlpha(0.7f + slideOffset);
        }
    };
    private ArrayList<MusicDataCapsule> dataList;
    private MusicMainAdapter adapter;
    private LinearLayout linearLayout;
    private RecyclerView recyclerView;
    private AudioManager audioManager;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private ProgressBar progressBar;
    private StorageUtil storageUtil;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ImageView navCover;
    private TextView navSongName, navArtistName;
    public BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                View miniPlayer = bottomSheetPlayerFragment.mini_play_view;
                View mainPlayer = bottomSheetPlayerFragment.player_layout;
                mainPlayer.setVisibility(View.INVISIBLE);
                miniPlayer.setVisibility(View.VISIBLE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                miniPlayer.setAlpha(1);
                shadowMain.setAlpha(0);
                mainPlayer.setAlpha(0);
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                View miniPlayer = bottomSheetPlayerFragment.mini_play_view;
                View mainPlayer = bottomSheetPlayerFragment.player_layout;
                miniPlayer.setVisibility(View.INVISIBLE);
                mainPlayer.setVisibility(View.VISIBLE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                miniPlayer.setAlpha(0);
                mainPlayer.setAlpha(1);
                shadowMain.setAlpha(1);
            } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                storageUtil.clearMusicLastPos();
                storageUtil.clearAudioIndex();
                storageUtil.clearMusicList();
                storageUtil.clearTempMusicList();
                resetDataInNavigation();
                bottomSheetPlayerFragment.resetMainPlayerLayout();
                stopMusic();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            View miniPlayer = bottomSheetPlayerFragment.mini_play_view;
            View mainPlayer = bottomSheetPlayerFragment.player_layout;
            miniPlayer.setVisibility(View.VISIBLE);
            mainPlayer.setVisibility(View.VISIBLE);
            miniPlayer.setAlpha(1 - slideOffset * 15);
            mainPlayer.setAlpha(0 + slideOffset * 3);
            shadowMain.setAlpha(0 + slideOffset);
        }
    };
    private boolean startPlaying;

    private View addPlayNextBtn, addToQueueBtn, setAsRingBtn;
    private MusicDataCapsule activeItem;
    private ImageView optionCover,addToFav;
    private TextView optionName, optionArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String switch1 = new StorageUtil.SettingsStorage(this).loadTheme();
        switch (switch1) {
            case system_follow:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case no_dark:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case dark:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
        setContentView(R.layout.activity_main);

        //initializations
        storageUtil = new StorageUtil(MainActivity.this);
        searchFragmentManager = getSupportFragmentManager();
        dataList = new ArrayList<>();

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

        MediaPlayerService.ui_visible = true;

        navigationView = findViewById(R.id.navigation_drawer);
        drawer = findViewById(R.id.drawer_layout);

        View headerView = navigationView.getHeaderView(0);
        View navDetailLayout = headerView.findViewById(R.id.nav_details_layout);
        navCover = headerView.findViewById(R.id.nav_cover_img);
        navSongName = headerView.findViewById(R.id.nav_song_name);
        navArtistName = headerView.findViewById(R.id.nav_song_artist);

        navDetailLayout.setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.START);
            if (getMusic() != null) {
                mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_toggle_open, R.string.drawer_toggle_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

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
        setBottomSheets();
        setFragmentInSlider();

        setUpOptionMenuButtons();

        lyricsListBehavior.addBottomSheetCallback(lrcFoundCallback);
        optionSheetBehavior.addBottomSheetCallback(optionCallback);
        mainPlayerSheetBehavior.addBottomSheetCallback(callback);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        player_bottom_sheet.setClickable(true);


        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.navigation_home);
        }

    }

    private void setUpOptionMenuButtons() {
        addPlayNextBtn = findViewById(R.id.add_play_next);
        addToQueueBtn = findViewById(R.id.add_to_queue);
        setAsRingBtn = findViewById(R.id.set_ringtone);
        optionCover = findViewById(R.id.song_album_cover_option);
        optionArtist = findViewById(R.id.song_artist_name_option);
        optionName = findViewById(R.id.song_name_option);
        addToFav = findViewById(R.id.add_to_favourites);

        addPlayNextBtn.setOnClickListener(v -> addToNextPlay());
        addToQueueBtn.setOnClickListener(v -> addToQueue());
        setAsRingBtn.setOnClickListener(v -> setAsRing());
        addToFav.setOnClickListener(v -> showToast("yeah"));
    }

    private void setBottomSheets() {
        mainPlayerSheetBehavior.setHideable(true);
        mainPlayerSheetBehavior.setPeekHeight(136);
        if (getMusic() != null) {
            mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        View optionSheet = findViewById(R.id.option_bottom_sheet);
        optionSheetBehavior = (CustomBottomSheet<View>) BottomSheetBehavior.from(optionSheet);
        optionSheetBehavior.setHideable(true);
        optionSheetBehavior.setPeekHeight(1100);
        optionSheetBehavior.setSkipCollapsed(true);
        optionSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        View lyricsListView = findViewById(R.id.found_lyrics_fragments);
        lyricsListBehavior = BottomSheetBehavior.from(lyricsListView);
        lyricsListBehavior.setHideable(true);
        lyricsListBehavior.setPeekHeight(360);
        lyricsListBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @SuppressLint("SetTextI18n")
    public void resetDataInNavigation() {
        navSongName.setText("Song Name");
        navArtistName.setText("Artist");
        GlideBuilt.glide(this, null, R.drawable.placeholder_nav, navCover, 400);
    }

    public void setDataInNavigation(String song_name, String artist_name, String album_uri) {
        navSongName.setText(song_name);
        navArtistName.setText(artist_name);
        GlideBuilt.glide(this, album_uri, R.drawable.ic_music, navCover, 400);
    }

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

    public void openBottomPlayer() {
        if (mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
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

    private void stopMusic() {
        if (!service_bound) {
            bindService();
            new Handler().postDelayed(() -> {
                //service is active send media with broadcast receiver
                Intent broadcastIntent = new Intent(BROADCAST_STOP_MUSIC);
                sendBroadcast(broadcastIntent);
            }, 0);
        } else {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_STOP_MUSIC);
            sendBroadcast(broadcastIntent);
        }
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

    public void setBottomSheetState() {
        lyricsListBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    protected void onStart() {
        if (is_granted) {
            bindService();
            //this will start playing song as soon as app starts if its connected to headset
            if (startPlaying)
                startSong();
        }
        super.onStart();
    }

    private void startSong() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (is_granted) {
                if (new StorageUtil.SettingsStorage(this).loadAutoPlay()) {
                    if (service_bound) {
                        if (!is_playing) {
                            //noinspection deprecation
                            if (audioManager.isBluetoothScoOn() || audioManager.isWiredHeadsetOn()) {
                                pausePlayAudio();
                            }

                        }
                    }
                }
            }
        }, 1500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (service_bound) {
            if (is_playing) {
                media_player_service.setSeekBar();
                EventBus.getDefault().post(new PrepareRunnableEvent());
            }
        }
    }

    @Override
    protected void onStop() {
        if (service_bound) {
            MainActivity.this.unbindService(service_connection);
        }
        startPlaying = false;
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (service_bound) {
            if (media_player_service.handler != null) {
                media_player_service.handler.removeCallbacks(media_player_service.runnable);
                EventBus.getDefault().post(new RemoveLyricsHandlerEvent());
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setSupportActionBar(null);
        if (service_bound) {
            //if media player is not playing it will stop the service
            if (media_player_service.media_player != null) {
                if (!is_playing) {
                    stopService(new Intent(MainActivity.this, MediaPlayerService.class));
                    service_bound = false;
                    is_playing = false;
                }
            }
        }
        MediaPlayerService.ui_visible = false;
        if (phoneStateListener != null && telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        searchFragmentManager = null;
    }

    /**
     * this function starts service and binds to MainActivity
     */
    private void bindService() {
        if (!phone_ringing) {
            Intent playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
            MainActivity.this.bindService(playerIntent, service_connection, Context.BIND_AUTO_CREATE);
            startService(playerIntent);
        } else {
            Toast.makeText(this, "Can't play while on call.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * play from start
     */
    public void playAudio() {
        //starting service if its not started yet otherwise it will send broadcast msg to service
        storageUtil.clearMusicLastPos();

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

    /**
     * play or pause
     */
    public void pausePlayAudio() {
        if (!phone_ringing) {
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
        } else {
            Toast.makeText(this, "Can't play while on call", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * next music
     */
    public void playNextAudio() {
        if (!phone_ringing) {
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
        } else {
            Toast.makeText(this, "Can't play while on call", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * previous music
     */
    public void playPreviousAudio() {
        if (!phone_ringing) {
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
        } else {
            Toast.makeText(this, "Can't play while on call", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * this function sets up player bottom sheet
     */
    private void setFragmentInSlider() {
        bottomSheetPlayerFragment = new BottomSheetPlayerFragment();
        FragmentManager mainPlayerManager = getSupportFragmentManager();
        FragmentTransaction transaction = mainPlayerManager.beginTransaction();
        transaction.replace(R.id.player_main_container, bottomSheetPlayerFragment);
        transaction.commit();
    }

    private void setSearchFragment() {
        SearchResultsFragment searchResultsFragment = new SearchResultsFragment();
        FragmentTransaction transaction = searchFragmentManager.beginTransaction();
        transaction.replace(R.id.sec_container, searchResultsFragment, "SearchResultsFragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setRingtone(MusicDataCapsule music) {
        Uri uri = Uri.fromFile(new File(music.getsPath()));
        Log.i("info", String.valueOf(uri));
        RingtoneManager.setActualDefaultRingtoneUri(MainActivity.this,
                RingtoneManager.TYPE_RINGTONE, uri);
        Uri uri2 = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
        Log.i("info", String.valueOf(uri2));
    }

    private void requestWriteSettingsPermission(MusicDataCapsule currentItem) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            boolean canWrite = Settings.System.canWrite(this);

            if (canWrite) {
                setRingtone(currentItem);
            } else {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }

    public void openOptionMenu(MusicDataCapsule currentItem) {
        activeItem = currentItem;
        optionSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        optionName.setText(currentItem.getsName());
        optionArtist.setText(currentItem.getsArtist());
        GlideBuilt.glide(this, currentItem.getsAlbumUri(), R.drawable.ic_music, optionCover, 65);
    }

    private void addToNextPlay() {
        showToast("Added to next");
        closeOptionSheet();
    }

    private void setAsRing() {
        requestWriteSettingsPermission(activeItem);
        closeOptionSheet();
    }

    private void addToQueue() {
        showToast("Added to queue");
        closeOptionSheet();
    }

    private void closeOptionSheet() {
        optionSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void showToast(String s) {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();

    }

    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //noinspection deprecation
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
                                //storageUtil.saveInitialMusicList(dataList);

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
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment1 = fragmentManager.findFragmentByTag("AboutFragment");
        Fragment fragment2 = fragmentManager.findFragmentByTag("SettingsFragment");

        if (mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED ||
                mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (lyricsListBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED ||
                        lyricsListBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED ||
                        lyricsListBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    lyricsListBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else {
                    if (fragment1 != null || fragment2 != null) {
                        navigationView.setCheckedItem(R.id.navigation_home);
                    }
                    super.onBackPressed();
                }
            }

        } else {
            if (bottomSheetPlayerFragment.queueSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetPlayerFragment.queueSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                if (mainPlayerSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                    mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
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
        SearchView searchView = (SearchView) searchViewItem.getActionView();

        searchView.setOnSearchClickListener(v -> setSearchFragment());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                EventBus.getDefault().post(new SearchEvent(query, dataList));
                return false;
            }

        });

        searchView.setOnCloseListener(() -> {
            searchFragmentManager.popBackStackImmediate();
            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment1 = fragmentManager.findFragmentByTag("AboutFragment");
        Fragment fragment2 = fragmentManager.findFragmentByTag("SettingsFragment");
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (item.getItemId()) {
            case R.id.navigation_home: {
                if (fragment1 != null || fragment2 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                break;
            }
            case R.id.navigation_setting: {
                if (fragment1 != null || fragment2 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                transaction.replace(R.id.sec_container, new SettingsFragment(), "SettingsFragment").addToBackStack(null).commit();
                break;
            }
            case R.id.navigation_about: {
                if (fragment1 != null || fragment2 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                transaction.replace(R.id.sec_container, new AboutFragment(), "AboutFragment").addToBackStack(null).commit();
                break;
            }
            case R.id.navigation_donate: {
                Toast.makeText(MainActivity.this, "donate $100 right now or else I will collect it in hell", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openBottomSheet(Bundle bundle) {
        setLyricListAdapter(bundle);
        lyricsListBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

}