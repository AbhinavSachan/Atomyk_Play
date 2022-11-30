package com.atomykcoder.atomykplay.activities;

import static com.atomykcoder.atomykplay.services.MediaPlayerService.is_playing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.FoundLyricsAdapter;
import com.atomykcoder.atomykplay.adapters.MusicMainAdapter;
import com.atomykcoder.atomykplay.adapters.PlaylistDialogAdapter;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.customScripts.CustomBottomSheet;
import com.atomykcoder.atomykplay.events.PrepareRunnableEvent;
import com.atomykcoder.atomykplay.events.RemoveLyricsHandlerEvent;
import com.atomykcoder.atomykplay.fragments.AboutFragment;
import com.atomykcoder.atomykplay.fragments.BottomSheetPlayerFragment;
import com.atomykcoder.atomykplay.fragments.FavoritesFragment;
import com.atomykcoder.atomykplay.fragments.HelpFragment;
import com.atomykcoder.atomykplay.fragments.PlaylistsFragment;
import com.atomykcoder.atomykplay.fragments.SearchFragment;
import com.atomykcoder.atomykplay.fragments.SettingsFragment;
import com.atomykcoder.atomykplay.helperFunctions.FetchMusic;
import com.atomykcoder.atomykplay.helperFunctions.PlaylistHelper;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.services.MediaPlayerService;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.atomykcoder.atomykplay.viewModals.Playlist;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

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
    public BottomSheetBehavior<View> donationSheetBehavior;
    public MusicDataCapsule optionItemSelected;
    FragmentManager searchFragmentManager;
    private View shadowMain;
    private View shadowLyrFound, shadowOuterSheet;
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
    private ArrayList<MusicDataCapsule> dataList;
    private MusicMainAdapter musicMainAdapter;
    private LinearLayout linearLayout;
    private RecyclerView musicRecyclerView;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private ProgressBar progressBar;
    private StorageUtil storageUtil;
    private DrawerLayout drawer;
    private final BottomSheetBehavior.BottomSheetCallback optionCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                shadowOuterSheet.setClickable(true);
                shadowOuterSheet.setFocusable(true);
                shadowOuterSheet.setAlpha(0.7f);
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                shadowOuterSheet.setClickable(true);
                shadowOuterSheet.setFocusable(true);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                shadowOuterSheet.setAlpha(1f);
            } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                shadowOuterSheet.setClickable(false);
                shadowOuterSheet.setFocusable(false);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            shadowOuterSheet.setAlpha(0.7f + slideOffset);
        }
    };
    private final BottomSheetBehavior.BottomSheetCallback donationCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                shadowOuterSheet.setClickable(true);
                shadowOuterSheet.setFocusable(true);
                shadowOuterSheet.setAlpha(0.6f);
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                shadowOuterSheet.setClickable(true);
                shadowOuterSheet.setFocusable(true);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                shadowOuterSheet.setAlpha(1f);
            } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                shadowOuterSheet.setClickable(false);
                shadowOuterSheet.setFocusable(false);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            shadowOuterSheet.setAlpha(0.6f + slideOffset);
        }
    };
    private NavigationView navigationView;
    private ImageView navCover;
    private TextView navSongName, navArtistName;
    private View anchoredShadow;
    public BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
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
                anchoredShadow.setAlpha(1);
                mainPlayer.setAlpha(0);
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                View miniPlayer = bottomSheetPlayerFragment.mini_play_view;
                View mainPlayer = bottomSheetPlayerFragment.player_layout;
                miniPlayer.setVisibility(View.INVISIBLE);
                mainPlayer.setVisibility(View.VISIBLE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                miniPlayer.setAlpha(0);
                mainPlayer.setAlpha(1);
                anchoredShadow.setAlpha(1);
                shadowMain.setAlpha(1);
            } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                anchoredShadow.setAlpha(0);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                clearStorage();
                bottomSheetPlayerFragment.resetMainPlayerLayout();
                resetDataInNavigation();
                stopMusic();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            View miniPlayer = bottomSheetPlayerFragment.mini_play_view;
            View mainPlayer = bottomSheetPlayerFragment.player_layout;
            miniPlayer.setVisibility(View.VISIBLE);
            mainPlayer.setVisibility(View.VISIBLE);
            miniPlayer.setAlpha(1 - slideOffset * 35);
            mainPlayer.setAlpha(0 + slideOffset);
            shadowMain.setAlpha(0 + slideOffset);
        }
    };
    //option menu buttons
    private View addPlayNextBtn, addToQueueBtn, addToPlaylist, setAsRingBtn, tagEditorBtn, addLyricsBtn, detailsBtn, shareBtn, deleteBtn;
    private ImageView optionCover, addToFav;
    private TextView optionName, optionArtist;
    private View optionSheet;
    private View donationSheet;
    private Runnable runnable;
    private View createPlaylistBtnDialog, addFavBtnDialog;
    public Dialog addToPlDialog;
    private PlaylistDialogAdapter playlistDialogAdapter;
    private ArrayList<Playlist> playlistArrayList;
    private RecyclerView plDialogRecyclerView;

    public void clearStorage() {
        storageUtil.clearMusicLastPos();
        storageUtil.clearAudioIndex();
        storageUtil.clearMusicList();
        storageUtil.clearTempMusicList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(this);
        boolean switch1 = settingsStorage.loadTheme();
        if (!switch1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        if (!settingsStorage.loadTheme()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            } else {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        }
        window.setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_main);

        //initializations
        storageUtil = new StorageUtil(MainActivity.this);
        searchFragmentManager = getSupportFragmentManager();
        dataList = storageUtil.loadInitialList();

        linearLayout = findViewById(R.id.song_not_found_layout);
        musicRecyclerView = findViewById(R.id.music_recycler);
        player_bottom_sheet = findViewById(R.id.player_main_container);
        progressBar = findViewById(R.id.progress_bar_main_activity);
        shadowMain = findViewById(R.id.shadow_main);
        shadowLyrFound = findViewById(R.id.shadow_lyrics_found);
        shadowOuterSheet = findViewById(R.id.outer_sheet_shadow);
        optionSheet = findViewById(R.id.option_bottom_sheet);
        donationSheet = findViewById(R.id.donation_bottom_sheet);
        anchoredShadow = findViewById(R.id.anchored_player_shadow);

        mainPlayerSheetBehavior = (CustomBottomSheet<View>) BottomSheetBehavior.from(player_bottom_sheet);

        ImageView openDrawer = findViewById(R.id.open_drawer_btn);
        MaterialCardView searchBar = findViewById(R.id.searchBar_card);
        View plCard = findViewById(R.id.playlist_card_view_ma);
        View lastAddCard = findViewById(R.id.last_added_card_view);
        View shuffleCard = findViewById(R.id.shuffle_play_card_view);

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

        openDrawer.setOnClickListener(v -> {
            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        searchBar.setOnClickListener(v -> setSearchFragment());
        plCard.setOnClickListener(v -> setPlaylistFragment());
        lastAddCard.setOnClickListener(v -> setLastAddFragment());
        shuffleCard.setOnClickListener(v -> playShuffleSong());

        musicRecyclerView.setHasFixedSize(true);

//        DragScrollBar scrollBar = findViewById(R.id.dragScrollBar);
//        scrollBar.setRecyclerView(musicRecyclerView);
//        scrollBar.setIndicator(new AlphabetIndicator(MainActivity.this), false);

        //Checking storage & other permissions before activity creation (method somewhere down in the script)
        checkPermission();
        if (is_granted) {
            callStateListener();
        }
        setFragmentInSlider();
        setBottomSheets();

        setUpOptionMenuButtons();

        if (is_granted) {
            if (dataList != null) {
                Handler handler = new Handler(Looper.getMainLooper());
                runnable = () -> {
                    checkForUpdateMusic();
                    handler.postDelayed(runnable, 30000);
                };
                handler.postDelayed(runnable, 5000);
            }
        }
        lyricsListBehavior.addBottomSheetCallback(lrcFoundCallback);
        optionSheetBehavior.addBottomSheetCallback(optionCallback);
        donationSheetBehavior.addBottomSheetCallback(donationCallback);
        mainPlayerSheetBehavior.addBottomSheetCallback(bottomSheetCallback);

        player_bottom_sheet.setClickable(true);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.navigation_home);
        }
    }

    private void playShuffleSong() {

    }

    private void setLastAddFragment() {

    }

    private void setPlaylistFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        PlaylistsFragment fragment = new PlaylistsFragment();
        fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_bottom));
        transaction.replace(R.id.sec_container, fragment, "PlaylistsFragment").addToBackStack(null).commit();
    }

    private void checkForUpdateMusic() {

        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        // do in background code here
        service.execute(() -> {
            ArrayList<MusicDataCapsule> updatedList = new ArrayList<>();
            FetchMusic.fetchMusic(updatedList, MainActivity.this);
            // post-execute code here
            handler.post(() -> {
                //Setting up adapter
                if (updatedList.isEmpty()) {
                    linearLayout.setVisibility(View.VISIBLE);
                } else {
                    linearLayout.setVisibility(View.GONE);
                }
                musicMainAdapter.updateMusicListItems(updatedList);
                storageUtil.saveInitialList(updatedList);
            });
        });
        service.shutdown();

    }

    private void setUpOptionMenuButtons() {
        addPlayNextBtn = findViewById(R.id.add_play_next_option);
        addToQueueBtn = findViewById(R.id.add_to_queue_option);
        addToPlaylist = findViewById(R.id.add_to_playlist_option);
        setAsRingBtn = findViewById(R.id.set_ringtone_option);
        tagEditorBtn = findViewById(R.id.tagEditor_option);
        addLyricsBtn = findViewById(R.id.addLyrics_option);
        detailsBtn = findViewById(R.id.details_option);
        shareBtn = findViewById(R.id.share_music_option);
        deleteBtn = findViewById(R.id.delete_music_option);
        optionCover = findViewById(R.id.song_album_cover_option);
        optionArtist = findViewById(R.id.song_artist_name_option);
        optionName = findViewById(R.id.song_name_option);
        addToFav = findViewById(R.id.add_to_favourites_option);

        addPlayNextBtn.setOnClickListener(this);
        addToQueueBtn.setOnClickListener(this);
        addToPlaylist.setOnClickListener(this);
        setAsRingBtn.setOnClickListener(this);
        tagEditorBtn.setOnClickListener(this);
        addLyricsBtn.setOnClickListener(this);
        detailsBtn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        addToFav.setOnClickListener(this);
    }

    private void setBottomSheets() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int peekHeight = (int) (metrics.heightPixels / 10.6f);
        mainPlayerSheetBehavior.setHideable(true);
        mainPlayerSheetBehavior.setPeekHeight(peekHeight);

        if (getMusic() != null) {
            mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            anchoredShadow.setAlpha(0);
            mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

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


        donationSheetBehavior = BottomSheetBehavior.from(donationSheet);
        donationSheetBehavior.setHideable(true);
        donationSheetBehavior.setPeekHeight(900);
        donationSheetBehavior.setSkipCollapsed(true);
        donationSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //this function will collapse the option bottom sheet if we click outside of sheet
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (optionSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                Rect outRect = new Rect();
                optionSheet.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    optionSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
            if (donationSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                Rect outRect = new Rect();
                donationSheet.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    donationSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @SuppressLint("SetTextI18n")
    public void resetDataInNavigation() {
        navSongName.setText("Song Name");
        navArtistName.setText("Artist");
        GlideBuilt.glide(this, null, R.drawable.placeholder_nav, navCover, 300);
    }

    public void setDataInNavigation(String song_name, String artist_name, String album_uri) {
        navSongName.setText(song_name);
        navArtistName.setText(artist_name);
        GlideBuilt.glide(this, album_uri, R.drawable.ic_music, navCover, 300);
    }

    private MusicDataCapsule getMusic() {
        ArrayList<MusicDataCapsule> musicList = storageUtil.loadMusicList();
        MusicDataCapsule activeMusic = null;
        int musicIndex;
        musicIndex = storageUtil.loadMusicIndex();

        if (musicList != null && musicList.size() != 0)
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

    public void stopMusic() {
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
        }
        super.onStart();
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
        SearchFragment searchFragment = new SearchFragment();
        FragmentTransaction transaction = searchFragmentManager.beginTransaction();

        searchFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
        transaction.replace(R.id.sec_container, searchFragment, "SearchResultsFragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setRingtone(MusicDataCapsule music) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean canWrite = Settings.System.canWrite(this);

            if (canWrite) {
                Uri newUri = Uri.fromFile(new File(music.getsPath()));
                RingtoneManager.setActualDefaultRingtoneUri(MainActivity.this, RingtoneManager.TYPE_RINGTONE, newUri);
                showToast("Ringtone set successfully");
            } else {
                requestWriteSettingsPermission();
            }
        }
    }

    private void requestWriteSettingsPermission() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.setAction(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    public void openOptionMenu(MusicDataCapsule currentItem) {
        optionItemSelected = currentItem;
        optionSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        optionName.setText(currentItem.getsName());
        optionArtist.setText(currentItem.getsArtist());
        GlideBuilt.glide(this, currentItem.getsAlbumUri(), R.drawable.ic_music, optionCover, 65);
    }

    /**
     * delete song permanently from device *USE WITH CAUTION*
     *
     * @param music music to be deleted
     */
    private void deleteFromDevice(MusicDataCapsule music) {
        Uri contentUri = getContentUri(music);
        ContentResolver contentResolver = getContentResolver();
        Uri normalUri = Uri.fromFile(new File(music.getsPath()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // for android 11 and above
            if (contentUri != null) {
                List<Uri> uris = new ArrayList<>(1);
                uris.add(contentUri);
                PendingIntent pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris);
                try {
                    //noinspection deprecation
                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            4494, null, 0, 0,
                            0, null);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            //for android 10
            if (normalUri != null) {
                try {
                    contentResolver.delete(normalUri, null, null);
                }
                // for android 10 we must catch a recoverable security exception
                catch (RecoverableSecurityException e) {
                    final IntentSender intent = e.getUserAction().getActionIntent().getIntentSender();
                    try {
                        //noinspection deprecation
                        startIntentSenderForResult(intent, 4494, null, 0,
                                0, 0, null);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } else {
            // for older devices
            if (normalUri != null) {
                contentResolver.delete(normalUri, null, null);
                musicMainAdapter.removeItem(optionItemSelected);
            }
        }
        closeOptionSheet();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //yeah it has to be in reverse for some reason to work
        if (resultCode == PackageManager.PERMISSION_DENIED) {
            musicMainAdapter.removeItem(optionItemSelected);
        }
    }

    private void addToNextPlay(MusicDataCapsule optionItemSelected) {
        showToast("Added to next");

    }

    private void addToQueue(MusicDataCapsule music) {
        showToast("Added to queue");
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
                        scanAndSetMusicAdapter();
                        is_granted = true;
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                        is_granted = false;
                    }
                }).check();

    }

    private void scanAndSetMusicAdapter() {
        //Fetch Music List along with it's metadata and save it in "dataList"

        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        progressBar.setVisibility(View.VISIBLE);
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);

        if (dataList != null) {
            //Setting up adapter
            if (dataList.isEmpty()) {
                linearLayout.setVisibility(View.VISIBLE);
            } else {
                linearLayout.setVisibility(View.GONE);
            }
            progressBar.setVisibility(View.GONE);
            musicMainAdapter = new MusicMainAdapter(MainActivity.this, dataList);
            musicRecyclerView.setAdapter(musicMainAdapter);
            musicRecyclerView.setLayoutManager(manager);
        } else {
            // do in background code here
            service.execute(() -> {
                dataList = new ArrayList<>();
                FetchMusic.fetchMusic(dataList, MainActivity.this);
                // post-execute code here
                handler.post(() -> {
                    //Setting up adapter
                    if (dataList.isEmpty()) {
                        linearLayout.setVisibility(View.VISIBLE);
                    } else {
                        linearLayout.setVisibility(View.GONE);
                        storageUtil.saveInitialList(dataList);
                    }
                    progressBar.setVisibility(View.GONE);
                    musicMainAdapter = new MusicMainAdapter(MainActivity.this, dataList);
                    musicRecyclerView.setAdapter(musicMainAdapter);
                    musicRecyclerView.setLayoutManager(manager);
                });
            });
            service.shutdown();
        }

    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment3 = fragmentManager.findFragmentByTag("SettingsFragment");
        Fragment fragment4 = fragmentManager.findFragmentByTag("HelpFragment");
        Fragment fragment5 = fragmentManager.findFragmentByTag("AboutFragment");

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
                    if ( fragment3 != null || fragment4 != null || fragment5 != null) {
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment3 = fragmentManager.findFragmentByTag("SettingsFragment");
        Fragment fragment4 = fragmentManager.findFragmentByTag("HelpFragment");
        Fragment fragment5 = fragmentManager.findFragmentByTag("AboutFragment");

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        switch (item.getItemId()) {
            case R.id.navigation_home: {
                if ( fragment3 != null || fragment4 != null || fragment5 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                break;
            }
            case R.id.navigation_setting: {
                if ( fragment3 != null || fragment4 != null || fragment5 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                SettingsFragment fragment = new SettingsFragment();
                fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_right));

                transaction.replace(R.id.sec_container, fragment, "SettingsFragment").addToBackStack(null).commit();
                break;
            }
            case R.id.navigation_help: {
                if ( fragment3 != null || fragment4 != null || fragment5 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                HelpFragment fragment = new HelpFragment();
                fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_right));

                transaction.replace(R.id.sec_container, fragment, "HelpFragment").addToBackStack(null).commit();
                break;
            }
            case R.id.navigation_about: {
                if ( fragment3 != null || fragment4 != null || fragment5 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                AboutFragment fragment = new AboutFragment();
                fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_right));

                transaction.replace(R.id.sec_container, fragment, "AboutFragment").addToBackStack(null).commit();
                break;
            }
            case R.id.navigation_donate: {
                donationSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            }
        }

        return true;
    }

    public void openBottomSheet(Bundle bundle) {
        setLyricListAdapter(bundle);
        lyricsListBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_play_next_option: {
                addToNextPlay(optionItemSelected);
                break;
            }
            case R.id.add_to_queue_option: {
                addToQueue(optionItemSelected);
                break;
            }
            case R.id.add_to_playlist_option: {
                addToPlaylist(optionItemSelected);
                break;
            }
            case R.id.set_ringtone_option: {
                setRingtone(optionItemSelected);
                break;
            }
            case R.id.delete_music_option: {
                deleteFromDevice(optionItemSelected);
                break;
            }

            case R.id.tagEditor_option: {
                openTagEditor(optionItemSelected);
                break;
            }
            case R.id.addLyrics_option: {
                bottomSheetPlayerFragment.setLyricsLayout(optionItemSelected);
                break;
            }
            case R.id.details_option: {
                openDetailsBox(optionItemSelected);
                break;
            }
            case R.id.share_music_option: {
                openShare(optionItemSelected);
                break;
            }
            case R.id.add_to_favourites_option: {
                addToFavorite(optionItemSelected);
                break;
            }
        }
        closeOptionSheet();
    }

    private void addToPlaylist(MusicDataCapsule optionItemSelected) {
        openPlaylistDialog(optionItemSelected);
    }

    private void openPlaylistDialog(MusicDataCapsule optionItemSelected) {
        addToPlDialog = new Dialog(MainActivity.this);

        addToPlDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addToPlDialog.setContentView(R.layout.playlist_dialog_layout);

        //Initialize Dialogue Box UI Items
        playlistArrayList = storageUtil.getAllPlaylist();
        plDialogRecyclerView = addToPlDialog.findViewById(R.id.playlist_dialog_recycler);
        createPlaylistBtnDialog = addToPlDialog.findViewById(R.id.create_playlist);
        addFavBtnDialog = addToPlDialog.findViewById(R.id.add_to_fav_dialog_box);

        createPlaylistBtnDialog.setOnClickListener(v -> {
            openCreatePlaylistDialog(optionItemSelected);
        });
        addFavBtnDialog.setOnClickListener(v -> {
            addToFavorite(optionItemSelected);
        });

        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
        plDialogRecyclerView.setLayoutManager(manager);

        if (playlistArrayList != null) {
            playlistDialogAdapter = new PlaylistDialogAdapter(this, playlistArrayList,optionItemSelected);
            plDialogRecyclerView.setAdapter(playlistDialogAdapter);
        }
        addToPlDialog.show();
    }

    private void addToFavorite(MusicDataCapsule optionItemSelected) {

    }

    @SuppressLint("NotifyDataSetChanged")
    private void openCreatePlaylistDialog(MusicDataCapsule optionItemSelected) {
        Dialog plDialog = new Dialog(MainActivity.this);
        plDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        plDialog.setContentView(R.layout.create_playlist_layout);

        EditText editText = plDialog.findViewById(R.id.edit_playlist_name);
        Button btnOk = plDialog.findViewById(R.id.btn_ok_pl);
        Button btnCancel = plDialog.findViewById(R.id.btn_cancel_pl);

        btnOk.setOnClickListener(v -> {
            String plKey = editText.getText().toString().trim();
            if (!plKey.equals("")) {
                storageUtil.createPlayList(plKey);
                ArrayList<Playlist> allList = storageUtil.getAllPlaylist();
                if (playlistArrayList != null) {
                    playlistArrayList.clear();
                    playlistArrayList.addAll(allList);
                    playlistDialogAdapter.notifyDataSetChanged();
                } else {
                    playlistDialogAdapter = new PlaylistDialogAdapter(this, allList,optionItemSelected);
                    plDialogRecyclerView.setAdapter(playlistDialogAdapter);
                }
                plDialog.cancel();
            } else {
                editText.setError("Empty");
            }
        });
        btnCancel.setOnClickListener(v -> plDialog.cancel());

        plDialog.show();
    }


    private void openShare(MusicDataCapsule itemOptionSelectedMusic) {
        Uri uri = Uri.parse(itemOptionSelectedMusic.getsPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share Via ..."));
    }

    private void openDetailsBox(MusicDataCapsule optionItemSelected) {

    }

    private void openTagEditor(MusicDataCapsule optionItemSelected) {

    }

    /**
     * get Content uri of music (Required to delete files in android 11 and above)
     *
     * @param music music
     * @return returns content uri of given music
     */
    private Uri getContentUri(MusicDataCapsule music) {
        int id = Integer.parseInt(music.getsId());
        Uri baseUri = Uri.parse("content://media/external/audio/media");
        return Uri.withAppendedPath(baseUri, "" + id);

    }
}