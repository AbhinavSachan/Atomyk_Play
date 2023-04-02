package com.atomykcoder.atomykplay.activities;

import static com.atomykcoder.atomykplay.enums.OptionSheetEnum.FAVORITE_LIST;
import static com.atomykcoder.atomykplay.enums.OptionSheetEnum.OPEN_PLAYLIST;
import static com.atomykcoder.atomykplay.helperFunctions.CustomMethods.pickImage;
import static com.atomykcoder.atomykplay.services.MediaPlayerService.is_playing;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
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
import com.atomykcoder.atomykplay.classes.PhoneStateCallback;
import com.atomykcoder.atomykplay.customScripts.CustomBottomSheet;
import com.atomykcoder.atomykplay.customScripts.LinearLayoutManagerWrapper;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.dataModels.Playlist;
import com.atomykcoder.atomykplay.enums.OptionSheetEnum;
import com.atomykcoder.atomykplay.events.PrepareRunnableEvent;
import com.atomykcoder.atomykplay.events.RemoveFromFavoriteEvent;
import com.atomykcoder.atomykplay.events.RemoveFromPlaylistEvent;
import com.atomykcoder.atomykplay.events.RemoveLyricsHandlerEvent;
import com.atomykcoder.atomykplay.fragments.AboutFragment;
import com.atomykcoder.atomykplay.fragments.BottomSheetPlayerFragment;
import com.atomykcoder.atomykplay.fragments.LastAddedFragment;
import com.atomykcoder.atomykplay.fragments.PlaylistsFragment;
import com.atomykcoder.atomykplay.fragments.SearchFragment;
import com.atomykcoder.atomykplay.fragments.SettingsFragment;
import com.atomykcoder.atomykplay.fragments.TagEditorFragment;
import com.atomykcoder.atomykplay.helperFunctions.Logger;
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.repository.MusicUtils;
import com.atomykcoder.atomykplay.services.MediaPlayerService;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {


    public static final String BROADCAST_PLAY_NEW_MUSIC = "com.atomykcoder.atomykplay.PlayNewMusic";
    public static final String BROADCAST_PAUSE_PLAY_MUSIC = "com.atomykcoder.atomykplay.PausePlayMusic";
    public static final String BROADCAST_STOP_MUSIC = "com.atomykcoder.atomykplay.StopMusic";
    public static final String BROADCAST_PLAY_NEXT_MUSIC = "com.atomykcoder.atomykplay.PlayNextMusic";
    public static final String BROADCAST_PLAY_PREVIOUS_MUSIC = "com.atomykcoder.atomykplay.PlayPreviousMusic";
    public static final int TAG_BLOCK_LIST = 2153;
    public static boolean service_bound = false;
    public static boolean is_granted = false;
    public static boolean phone_ringing = false;
    public static boolean service_stopped = false;
    private GlideBuilt glideBuilt;
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
    public static final String SETTINGS_FRAGMENT_TAG = "SettingsFragment";
    public static final String ADD_LYRICS_FRAGMENT_TAG = "AddLyricsFragment";
    public static final String SEARCH_FRAGMENT_TAG = "SearchFragment";
    public static final String FAVORITE_FRAGMENT_TAG = "FavoritesFragment";
    public static final String TAG_EDITOR_FRAGMENT_TAG = "TagEditorFragment";
    public static final String PLAYLISTS_FRAGMENT_TAG = "PlaylistsFragment";
    public static final String ABOUT_FRAGMENT_TAG = "AboutFragment";
    public static final String OPEN_PLAYLIST_FRAGMENT_TAG = "OpenPlayListFragment";
    public static final String LAST_ADDED_FRAGMENT_TAG = "LastAddedFragment";

    public CustomBottomSheet<View> mainPlayerSheetBehavior;
    public BottomSheetPlayerFragment bottomSheetPlayerFragment;
    public LastAddedFragment lastAddedFragment;
    public BottomSheetBehavior<View> lyricsListBehavior;
    public BottomSheetBehavior<View> optionSheetBehavior;
    public BottomSheetBehavior<View> donationSheetBehavior;
    public BottomSheetBehavior<View> detailsSheetBehavior;
    public BottomSheetBehavior<View> plSheetBehavior;
    public AlertDialog addToPlDialog;
    public View plSheet, player_bottom_sheet;
    public Playlist plItemSelected;
    public Music selectedItem;
    public boolean isChecking = false;
    private View shadowLyrFound;
    private final BottomSheetBehavior.BottomSheetCallback lrcFoundCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED:
                    shadowLyrFound.setAlpha(0.2f);
                    lyricsSheet.setElevation(4f);
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    shadowLyrFound.setAlpha(1f);
                    lyricsSheet.setElevation(4f);
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    lyricsSheet.setElevation(0f);
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            shadowLyrFound.setAlpha(0.2f + slideOffset);
        }
    };
    private View shadowOuterSheet;
    private View shadowOuterSheet2;
    private View shadowMain;
    private View anchoredShadow;
    private ImageView playlist_image_View, navCover;
    private Uri playListImageUri;
    private MusicMainAdapter musicMainAdapter;
    private LinearLayout linearLayout;
    private RecyclerView musicRecyclerView;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private PhoneStateCallback phoneStateCallback;
    private ProgressBar progressBar;
    private StorageUtil storageUtil;
    private DrawerLayout drawer;
    private final BottomSheetBehavior.BottomSheetCallback optionCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED:
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    shadowOuterSheet.setClickable(true);
                    shadowOuterSheet.setFocusable(true);
                    shadowOuterSheet.setAlpha(0.7f);
                    optionSheet.setElevation(18f);
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    shadowOuterSheet.setClickable(true);
                    shadowOuterSheet.setFocusable(true);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    shadowOuterSheet.setAlpha(1f);
                    optionSheet.setElevation(18f);
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    shadowOuterSheet.setClickable(false);
                    shadowOuterSheet.setFocusable(false);
                    optionSheet.setElevation(0f);
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            shadowOuterSheet.setAlpha(0.7f + slideOffset);
        }
    };
    private final BottomSheetBehavior.BottomSheetCallback detailsSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED:
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    shadowOuterSheet2.setClickable(true);
                    shadowOuterSheet2.setFocusable(true);
                    shadowOuterSheet2.setAlpha(0.45f);
                    detailsSheet.setElevation(20f);
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    shadowOuterSheet2.setClickable(true);
                    shadowOuterSheet2.setFocusable(true);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    shadowOuterSheet2.setAlpha(1f);
                    detailsSheet.setElevation(20f);
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    shadowOuterSheet2.setClickable(false);
                    shadowOuterSheet2.setFocusable(false);
                    detailsSheet.setElevation(0f);
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            shadowOuterSheet2.setAlpha(0.45f + slideOffset);
        }
    };
    private final BottomSheetBehavior.BottomSheetCallback donationCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED:
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    shadowOuterSheet.setClickable(true);
                    shadowOuterSheet.setFocusable(true);
                    shadowOuterSheet.setAlpha(0.6f);
                    donationSheet.setElevation(16f);
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    shadowOuterSheet.setClickable(true);
                    shadowOuterSheet.setFocusable(true);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    shadowOuterSheet.setAlpha(1f);
                    donationSheet.setElevation(16f);
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    shadowOuterSheet.setClickable(false);
                    shadowOuterSheet.setFocusable(false);
                    donationSheet.setElevation(0f);
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            shadowOuterSheet.setAlpha(0.6f + slideOffset);
        }
    };

    private final BottomSheetBehavior.BottomSheetCallback plSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED:
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    shadowOuterSheet.setClickable(true);
                    shadowOuterSheet.setFocusable(true);
                    shadowOuterSheet.setAlpha(0.6f);
                    plSheet.setElevation(18f);
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    shadowOuterSheet.setClickable(true);
                    shadowOuterSheet.setFocusable(true);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    plSheet.setElevation(18f);
                    shadowOuterSheet.setAlpha(1f);
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    shadowOuterSheet.setClickable(false);
                    shadowOuterSheet.setFocusable(false);
                    plSheet.setElevation(0f);
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            shadowOuterSheet.setAlpha(0.6f + slideOffset);
        }
    };
    private NavigationView navigationView;
    private int tempThemeColor;
    private TextView navSongName, navArtistName;
    public BottomSheetBehavior.BottomSheetCallback mainPlayerSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED: {
                    View miniPlayer = bottomSheetPlayerFragment.mini_play_view;
                    View mainPlayer = bottomSheetPlayerFragment.player_layout;
                    mainPlayer.setVisibility(View.INVISIBLE);
                    miniPlayer.setVisibility(View.VISIBLE);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    miniPlayer.setAlpha(1);
                    shadowMain.setAlpha(0);
                    anchoredShadow.setAlpha(1);
                    mainPlayer.setAlpha(0);
                    anchoredShadow.setElevation(10f);
                    player_bottom_sheet.setElevation(12f);
                    changeNavigationColor(tempThemeColor, getResources().getColor(R.color.player_bg, null));
                    tempThemeColor = getResources().getColor(R.color.player_bg, null);
                    break;
                }
                case BottomSheetBehavior.STATE_EXPANDED: {
                    View miniPlayer = bottomSheetPlayerFragment.mini_play_view;
                    View mainPlayer = bottomSheetPlayerFragment.player_layout;
                    miniPlayer.setVisibility(View.INVISIBLE);
                    mainPlayer.setVisibility(View.VISIBLE);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    miniPlayer.setAlpha(0);
                    mainPlayer.setAlpha(1);
                    anchoredShadow.setAlpha(1);
                    shadowMain.setAlpha(1);
                    anchoredShadow.setElevation(10f);
                    player_bottom_sheet.setElevation(12f);
                    bottomSheetPlayerFragment.getThemeColor().observe(MainActivity.this, it -> {
                        if (mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            changeNavigationColor(tempThemeColor, it);
                            tempThemeColor = it;
                        }
                    });
                    break;
                }
                case BottomSheetBehavior.STATE_HIDDEN:
                    anchoredShadow.setAlpha(0);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    player_bottom_sheet.setElevation(0f);
                    anchoredShadow.setElevation(0f);
                    clearStorage();
                    bottomSheetPlayerFragment.queueAdapter.clearList();
                    bottomSheetPlayerFragment.resetMainPlayerLayout();
                    resetDataInNavigation();
                    stopMusic();
                    break;
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
    private View deleteBtn;
    private View optionSheet, detailsSheet, donationSheet;
    private View removeFromList;
    private ImageView optionCover, plOptionCover, addToFav;
    private TextView optionName, optionArtist;
    private TextView plOptionName, optionPlCount;
    private PlaylistDialogAdapter playlistDialogAdapter;
    private ArrayList<Playlist> playlistArrayList;
    private RecyclerView plDialogRecyclerView;
    private PlaylistsFragment playlistFragment;
    private SearchFragment searchFragment;
    private AlertDialog ringtoneDialog = null;
    private String pl_name;
    private OptionSheetEnum optionTag;
    private TextView songPathTv, songNameTv, songArtistTv, songSizeTv, songGenreTv, songBitrateTv, songAlbumTv;
    private Handler handler;
    private MusicUtils musicUtils;

    // Create a new ActivityResultLauncher to handle the delete request result
    private final ActivityResultLauncher<IntentSenderRequest> deleteMusicRequestLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            // Delete request was successful
            updateAdaptersForRemovedItem();
        } else {
            // Delete request failed
            showToast("Failed to delete this song");
        }
    });


    // Registers a photo picker activity launcher in single-select mode.
    private final ActivityResultLauncher<PickVisualMediaRequest> mediaPickerForPLCover = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            playListImageUri = uri;
            playlist_image_View.setImageURI(playListImageUri);

        }
    });

    private final ActivityResultLauncher<Intent> pickIntentForPLCover = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                playListImageUri = result.getData().getData();
                playlist_image_View.setImageURI(playListImageUri);

            }
        }
    });
    // Registers a photo picker activity launcher in single-select mode.
    private final ActivityResultLauncher<PickVisualMediaRequest> mediaPickerForPLCoverChange = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            String coverImageUri = uri.toString();
            storageUtil.replacePlaylist(storageUtil.loadPlaylist(pl_name), pl_name, coverImageUri);
            if (playlistFragment != null && playlistFragment.playlistAdapter != null) {
                playlistFragment.playlistAdapter.updateView(storageUtil.getAllPlaylist());
            }

        }
    });

    private final ActivityResultLauncher<Intent> pickIntentForPLCoverChange = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                String coverImageUri = result.getData().getData().toString();
                storageUtil.replacePlaylist(storageUtil.loadPlaylist(pl_name), pl_name, coverImageUri);
                if (playlistFragment != null && playlistFragment.playlistAdapter != null) {
                    playlistFragment.playlistAdapter.updateView(storageUtil.getAllPlaylist());
                }
            }
        }
    });
    private View lyricsSheet;
    private long lastClickTime;

    public void clearStorage() {
        storageUtil.clearMusicLastPos();
        storageUtil.clearMusicIndex();
        storageUtil.clearQueueList();
        storageUtil.clearTempMusicList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(this);
        boolean switch1 = settingsStorage.loadIsThemeDark();
        if (!switch1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (!settingsStorage.loadIsThemeDark()) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        window.setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_main);

        //initializations
        MediaPlayerService.ui_visible = true;
        storageUtil = new StorageUtil(MainActivity.this);
        executorService = Executors.newFixedThreadPool(10);
        handler = new Handler(Looper.getMainLooper());
        musicUtils = MusicUtils.getInstance();
        glideBuilt = new GlideBuilt(this);
        tempThemeColor = getResources().getColor(R.color.player_bg, null);

        linearLayout = findViewById(R.id.song_not_found_layout);
        musicRecyclerView = findViewById(R.id.music_recycler);
        player_bottom_sheet = findViewById(R.id.player_main_container);
        progressBar = findViewById(R.id.progress_bar_main_activity);
        shadowMain = findViewById(R.id.shadow_main);
        shadowLyrFound = findViewById(R.id.shadow_lyrics_found);
        shadowOuterSheet = findViewById(R.id.outer_sheet_shadow);
        shadowOuterSheet2 = findViewById(R.id.outer_sheet_details_shadow);
        optionSheet = findViewById(R.id.option_bottom_sheet);
        donationSheet = findViewById(R.id.donation_bottom_sheet);
        plSheet = findViewById(R.id.pl_option_bottom_sheet);
        detailsSheet = findViewById(R.id.file_details_sheet);
        anchoredShadow = findViewById(R.id.anchored_player_shadow);

        mainPlayerSheetBehavior = (CustomBottomSheet<View>) BottomSheetBehavior.from(player_bottom_sheet);

        ImageView openDrawer = findViewById(R.id.open_drawer_btn);
        MaterialCardView searchBar = findViewById(R.id.searchBar_card);
        View plCard = findViewById(R.id.playlist_card_view_ma);
        View lastAddCard = findViewById(R.id.last_added_card_view);
        View shuffleCard = findViewById(R.id.shuffle_play_card_view);

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

        //Checking storage & other permissions
        checkForPermission();

        setFragmentInSlider();
        setBottomSheets();

        setUpOptionMenuButtons();
        setDetailsMenuButtons();
        setUpPlOptionMenuButtons();

        lyricsListBehavior.addBottomSheetCallback(lrcFoundCallback);
        optionSheetBehavior.addBottomSheetCallback(optionCallback);
        detailsSheetBehavior.addBottomSheetCallback(detailsSheetCallback);
        plSheetBehavior.addBottomSheetCallback(plSheetCallback);
        donationSheetBehavior.addBottomSheetCallback(donationCallback);
        mainPlayerSheetBehavior.addBottomSheetCallback(mainPlayerSheetCallback);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.navigation_home);
        }

    }


    private void checkForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                is_granted = false;
                showRequestDialog();
            } else {
                is_granted = true;
                setUpServiceAndScanner();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                is_granted = false;
                showRequestDialog();
            } else {
                is_granted = true;
                setUpServiceAndScanner();
            }
        }
    }

    private void showRequestDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View customLayout = getLayoutInflater().inflate(R.layout.permission_request_dialog_layout, null);
        builder.setView(customLayout);
        builder.setCancelable(true);
        builder.setPositiveButton("Allow", (dialogInterface, i) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestReadAudioAndPhonePermissionAbove12();
            } else {
                requestReadStorageAndPhonePermissionBelow12();
            }
        });
        builder.setOnCancelListener(dialog -> finish());
        androidx.appcompat.app.AlertDialog dialog;
        dialog = builder.create();
        dialog.show();
    }


    private void setDetailsMenuButtons() {
        songPathTv = findViewById(R.id.file_path_detail);
        songNameTv = findViewById(R.id.file_name_detail);
        songArtistTv = findViewById(R.id.file_artist_detail);
        songSizeTv = findViewById(R.id.file_size_detail);
        songGenreTv = findViewById(R.id.file_genre_detail);
        songBitrateTv = findViewById(R.id.file_bitrate_detail);
        songAlbumTv = findViewById(R.id.file_album_detail);
    }

    @Override
    protected void onStart() {
        super.onStart();

        MediaPlayerService.ui_visible = true;

        if (is_granted) {
            if (musicMainAdapter == null) {
                setUpMusicScanner();
            }
        }
        if (is_granted) {
            if (!isChecking) {
                checkForUpdateList(false);
            }
        }
        if (service_bound) {
            if (is_playing) {
                if (media_player_service!=null) {
                    media_player_service.setSeekBar();
                }
                EventBus.getDefault().post(new PrepareRunnableEvent());
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        MediaPlayerService.ui_visible = false;

        if (service_bound) {
            if (media_player_service != null) if (media_player_service.seekBarHandler != null) {
                media_player_service.seekBarHandler.removeCallbacks(media_player_service.seekBarRunnable);
                EventBus.getDefault().post(new RemoveLyricsHandlerEvent());
            }
        }
    }

    public void changeNavigationColor(int animateFrom, int animateTo) {
        Window window = getWindow();
        ValueAnimator colorAnimation = ValueAnimator.ofArgb(animateFrom, animateTo);
        colorAnimation.setDuration(100);
        colorAnimation.addUpdateListener(animator -> window.setNavigationBarColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaPlayerService.ui_visible = false;
        if (executorService != null) {
            executorService.shutdown();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (phoneStateCallback != null && telephonyManager != null) {
                telephonyManager.unregisterTelephonyCallback(phoneStateCallback);
            }
        } else {
            if (phoneStateListener != null && telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
        }
        if (service_bound) {
            MainActivity.this.unbindService(service_connection);
            Logger.normalLog("UnbindService"+" - MainActivity");
            stopMusicService();

        }
        playlistFragment = null;
        searchFragment = null;
        lastAddedFragment = null;
        bottomSheetPlayerFragment = null;
    }

    private void stopMusicService(){
        if (media_player_service != null){
            if (!media_player_service.isMediaPlaying()){
                stopService(new Intent(MainActivity.this, MediaPlayerService.class));
                Logger.normalLog("ServiceStopped"+" - MainActivity");
                service_stopped = true;
            }
        }
    }
    /**
     * this function starts service and binds to MainActivity
     */
    private void bindService() {
        Intent playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
        Logger.normalLog("ServiceBind"+" - MainActivity");
        MainActivity.this.bindService(playerIntent, service_connection, Context.BIND_AUTO_CREATE);
    }

    private void startService() {
        Intent playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
        Logger.normalLog("StartService"+" - MainActivity");
        try {
            startService(playerIntent);
            service_stopped = false;
        } catch (Exception e) {
            service_stopped = true;
        }

    }

    private void setUpPlOptionMenuButtons() {
        View addPlayNextPlBtn = findViewById(R.id.add_play_next_pl_option);
        View addToQueuePlBtn = findViewById(R.id.add_to_queue_pl_option);
        View nameEditorBtnPl = findViewById(R.id.rename_pl_option);
        View chooseCoverPl = findViewById(R.id.choose_cover_option);
        View deletePlBtn = findViewById(R.id.delete_pl_option);
        plOptionCover = findViewById(R.id.playlist_cover_option);
        plOptionName = findViewById(R.id.playlist_name_option);
        optionPlCount = findViewById(R.id.playlist_count_name_option);

        addPlayNextPlBtn.setOnClickListener(this);
        addToQueuePlBtn.setOnClickListener(this);
        nameEditorBtnPl.setOnClickListener(this);
        chooseCoverPl.setOnClickListener(this);
        deletePlBtn.setOnClickListener(this);

    }

    private void playShuffleSong() {
        ArrayList<Music> list = storageUtil.loadInitialList();
        if (list == null) {
            showToast("No Songs");
            return;
        }
        if (list.isEmpty()) {
            showToast("No Songs");
            return;
        }
        playRandomSong(list);
    }

    private void setLastAddFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        lastAddedFragment = (LastAddedFragment) fragmentManager.findFragmentByTag(LAST_ADDED_FRAGMENT_TAG);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (lastAddedFragment == null) {
            lastAddedFragment = new LastAddedFragment();
            lastAddedFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_bottom));
            transaction.replace(R.id.sec_container, lastAddedFragment, LAST_ADDED_FRAGMENT_TAG).addToBackStack(null).commit();
            navigationView.setCheckedItem(R.id.navigation_last_added);
        }
    }

    private void setPlaylistFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment3 = fragmentManager.findFragmentByTag(PLAYLISTS_FRAGMENT_TAG);

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        playlistFragment = new PlaylistsFragment();
        if (fragment3 == null) {
            playlistFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_right));
            transaction.replace(R.id.sec_container, playlistFragment, PLAYLISTS_FRAGMENT_TAG).addToBackStack(null).commit();
            navigationView.setCheckedItem(R.id.navigation_playlist);
        }
    }

    private ExecutorService executorService;

    private void setUpOptionMenuButtons() {
        //option menu buttons
        View addPlayNextBtn = findViewById(R.id.add_play_next_option);
        View addToQueueBtn = findViewById(R.id.add_to_queue_option);
        View addToPlaylist = findViewById(R.id.add_to_playlist_option);
        View setAsRingBtn = findViewById(R.id.set_ringtone_option);
        View tagEditorBtn = findViewById(R.id.tagEditor_option);
        View addLyricsBtn = findViewById(R.id.addLyrics_option);
        View detailsBtn = findViewById(R.id.details_option);
        View shareBtn = findViewById(R.id.share_music_option);
        deleteBtn = findViewById(R.id.delete_music_option);
        optionCover = findViewById(R.id.song_album_cover_option);
        optionArtist = findViewById(R.id.song_artist_name_option);
        optionName = findViewById(R.id.song_name_option);
        addToFav = findViewById(R.id.add_to_favourites_option);
        removeFromList = findViewById(R.id.remove_music_option);

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
        removeFromList.setOnClickListener(this);
    }

    private void setBottomSheetProperties(BottomSheetBehavior<View> sheet, int peekHeight, boolean skipCollapse) {
        sheet.setHideable(true);
        sheet.setPeekHeight(peekHeight);
        sheet.setSkipCollapsed(skipCollapse);
        sheet.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void setBottomSheets() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int mainPlayerPeekHeight = (int) (metrics.heightPixels / 10.6f);
        mainPlayerSheetBehavior.setHideable(true);
        mainPlayerSheetBehavior.setPeekHeight(mainPlayerPeekHeight);

        if (getMusic() != null) {
            mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            anchoredShadow.setAlpha(0);
            mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        int optionPeekHeight = (int) (metrics.heightPixels / 1.3f);
        optionSheetBehavior = BottomSheetBehavior.from(optionSheet);

        setBottomSheetProperties(optionSheetBehavior, optionPeekHeight, true);

        int detailsPeekHeight = (int) (metrics.heightPixels / 1.95f);
        detailsSheetBehavior = BottomSheetBehavior.from(detailsSheet);

        setBottomSheetProperties(detailsSheetBehavior, detailsPeekHeight, true);

        int lyricFoundPeekHeight = (int) (metrics.heightPixels / 3.5f);
        lyricsSheet = findViewById(R.id.found_lyrics_fragments);
        lyricsListBehavior = BottomSheetBehavior.from(lyricsSheet);

        setBottomSheetProperties(lyricsListBehavior, lyricFoundPeekHeight, false);


        int donationPeekHeight = (int) (metrics.heightPixels / 1.4f);
        donationSheetBehavior = BottomSheetBehavior.from(donationSheet);

        setBottomSheetProperties(donationSheetBehavior, donationPeekHeight, true);

        int plOptionPeekHeight = (int) (metrics.heightPixels / 1.8f);
        plSheetBehavior = BottomSheetBehavior.from(plSheet);

        setBottomSheetProperties(plSheetBehavior, plOptionPeekHeight, true);

    }

    private void closeSheetWhenClickOutSide(BottomSheetBehavior<View> sheetBehavior, View sheet, MotionEvent event) {
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            Rect outRect = new Rect();
            sheet.getGlobalVisibleRect(outRect);
            if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //this function will collapse the option bottom sheet if we click outside of sheet
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            closeSheetWhenClickOutSide(optionSheetBehavior, optionSheet, event);
            closeSheetWhenClickOutSide(detailsSheetBehavior, detailsSheet, event);
            closeSheetWhenClickOutSide(donationSheetBehavior, donationSheet, event);
            closeSheetWhenClickOutSide(plSheetBehavior, plSheet, event);
        }
        return super.dispatchTouchEvent(event);
    }

    @SuppressLint("SetTextI18n")
    public void resetDataInNavigation() {
        navSongName.setText("Song Name");
        navArtistName.setText("Artist");
        glideBuilt.glide(null, R.drawable.ic_music, navCover, 512);
    }

    public void setDataInNavigation(String song_name, String artist_name, Bitmap album_uri) {
        navSongName.setText(song_name);
        navArtistName.setText(artist_name);
        glideBuilt.glideBitmap(album_uri, R.drawable.ic_music, navCover, 512,false);
    }

    private Music getMusic() {
        ArrayList<Music> musicList = storageUtil.loadQueueList();
        Music activeMusic = null;
        int musicIndex;
        musicIndex = storageUtil.loadMusicIndex();

        if (musicList != null && musicList.size() != 0) {
            if (musicIndex != -1 && musicIndex < musicList.size()) {
                activeMusic = musicList.get(musicIndex);
            } else {
                activeMusic = musicList.get(0);
            }
        }

        return activeMusic;
    }

    public void openBottomPlayer() {
        if (mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    public void setLyricListAdapter(Bundle bundle) {
        ArrayList<String> titles = bundle.getStringArrayList("titles");
        ArrayList<String> sampleLyrics = bundle.getStringArrayList("sampleLyrics");
        ArrayList<String> urls = bundle.getStringArrayList("urls");

        RecyclerView recyclerView = findViewById(R.id.found_lyrics_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        FoundLyricsAdapter adapter = new FoundLyricsAdapter(titles, sampleLyrics, urls, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void stopMusic() {
        if (!service_stopped) {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_STOP_MUSIC);
            Logger.normalLog("MusicStop"+" - MainActivity");
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

    /**
     * play from start
     */
    public void playAudio(Music music) {
        //starting service if its not started yet otherwise it will send broadcast msg to service
        storageUtil.clearMusicLastPos();
        Logger.normalLog("MusicPlay"+" - MainActivity");
        String encodedMessage = MusicHelper.encode(music);

        if (!phone_ringing) {
            if (service_stopped) {
                startService();
                handler.postDelayed(() -> {
                    //service is active send media with broadcast receiver
                    Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_MUSIC);
                    broadcastIntent.putExtra("music", encodedMessage);
                    sendBroadcast(broadcastIntent);
                }, 0);
            } else {
                //service is active send media with broadcast receiver
                Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_MUSIC);
                broadcastIntent.putExtra("music", encodedMessage);
                sendBroadcast(broadcastIntent);
            }
        } else {
            showToast("Can't play while on call");
        }
    }


    public void playRandomSong(ArrayList<Music> songs) {
        if (shouldIgnoreClick()) {
            return;
        }
        storageUtil.saveShuffle(true);
        storageUtil.saveTempMusicList(songs);
        /*
         * Plays a random song from the given list of songs by sending a broadcast message
         */
        Random rand = new Random();
        Music music = songs.get(rand.nextInt(songs.size()));
        //removing current item from list
        songs.remove(music);

        //shuffling list
        Collections.shuffle(songs);

        //adding the removed item in shuffled list on 0th index
        songs.add(0, music);

        //saving list
        storageUtil.saveQueueList(songs);
        storageUtil.saveMusicIndex(0);
        bottomSheetPlayerFragment.updateQueueAdapter(songs);
        playAudio(music);
        openBottomPlayer();
    }

    /**
     * play or pause
     */
    public void pausePlayAudio() {
        if (!phone_ringing) {
            if (service_stopped) {
                startService();
                handler.postDelayed(() -> {
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
            showToast("Can't play while on call");
        }
        Logger.normalLog("MusicResume"+" - MainActivity");
    }
    private boolean shouldIgnoreClick(){
        long delay = 500;
        if (SystemClock.elapsedRealtime() < (lastClickTime + delay)) {
            return true;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        return false;
    }

    /**
     * next music
     */
    public void playNextAudio() {
        if (shouldIgnoreClick()) {
            return;
        }
        if (!phone_ringing) {
            if (service_stopped) {
                startService();
                handler.postDelayed(() -> {
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
            showToast("Can't play while on call");
        }
        Logger.normalLog("MusicNext"+" - MainActivity");
    }

    /**
     * previous music
     */
    public void playPreviousAudio() {
        if (shouldIgnoreClick()) {
            return;
        }
        if (!phone_ringing) {
            if (service_stopped) {
                startService();
                handler.postDelayed(() -> {
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
            showToast("Can't play while on call");
        }
        Logger.normalLog("MusicPrev"+" - MainActivity");
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
        searchFragment = new SearchFragment();
        replaceFragment(R.id.sec_container, searchFragment, android.R.transition.fade, SEARCH_FRAGMENT_TAG);
    }

    private void setRingtone(Music music) {

        boolean canWrite = Settings.System.canWrite(this);

        if (canWrite) {
            Uri newUri = Uri.fromFile(new File(music.getPath()));
            AlertDialog.Builder ringtoneDialog = new AlertDialog.Builder(MainActivity.this);
            ringtoneDialog.setTitle("Set as ringtone");
            ringtoneDialog.setMessage("Name - " + music.getName());
            ringtoneDialog.setCancelable(true);
            ringtoneDialog.setPositiveButton("OK", (dialog, which) -> {
                RingtoneManager.setActualDefaultRingtoneUri(MainActivity.this, RingtoneManager.TYPE_RINGTONE, newUri);
                showToast("Ringtone set successfully");
            });
            ringtoneDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            try {
                this.ringtoneDialog = ringtoneDialog.create();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (this.ringtoneDialog != null) {
                this.ringtoneDialog.show();
            }
        } else {
            requestWriteSettingsPermission();
        }

    }

    private void requestWriteSettingsPermission() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    public void openOptionMenu(Music music, OptionSheetEnum tag) {
        selectedItem = music;
        optionTag = tag;
        removeFromList.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);

        if (!storageUtil.checkFavourite(music)) {
            addToFav.setImageResource(R.drawable.ic_favorite_border);
        } else if (storageUtil.checkFavourite(music)) {
            addToFav.setImageResource(R.drawable.ic_favorite);
        }

        switch (tag) {
            case OPEN_PLAYLIST:
                removeFromList.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.GONE);
                break;
            case FAVORITE_LIST:
                break;
            case MAIN_LIST:
                removeFromList.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.VISIBLE);
                break;
        }
        executorService.execute(() -> {
            Bitmap image = null;
            //image decoder
            try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
                mediaMetadataRetriever.setDataSource(music.getPath());
                byte[] art = mediaMetadataRetriever.getEmbeddedPicture();

                try {
                    image = BitmapFactory.decodeByteArray(art, 0, art.length);
                } catch (Exception ignored) {
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap finalImage = image;
            runOnUiThread(() -> {
                optionSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                optionName.setText(music.getName());
                optionArtist.setText(music.getArtist());
                glideBuilt.glideBitmap(finalImage, R.drawable.ic_music, optionCover, 128,false);
            });
        });

    }

    public void openPlOptionMenu(Playlist currentItem) {
        plItemSelected = currentItem;
        String count = currentItem.getMusicList().size() + " Songs";
        plSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        plOptionName.setText(currentItem.getName());
        optionPlCount.setText(count);
        glideBuilt.glide(currentItem.getCoverUri(), R.drawable.ic_music_list, plOptionCover, 128);
    }

    private void deleteFromDevice(List<Music> musics) {

        ContentResolver contentResolver = getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            List<Uri> uris = new ArrayList<>();
            for (Music music : musics) {
                Uri contentUri = getContentUri(music);
                if (contentUri != null) {
                    uris.add(contentUri);
                }
            }
            // for android 11 and above
            if (!uris.isEmpty()) {
                // Create a PendingIntent for the delete request
                PendingIntent pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris);

                // Create an IntentSenderRequest for the delete request
                IntentSenderRequest request = new IntentSenderRequest.Builder(pendingIntent.getIntentSender()).build();

                // Launch the delete request using the ActivityResultLauncher
                deleteMusicRequestLauncher.launch(request);
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // for android 10
            for (Music music : musics) {
                try {
                    deleteItemWithContentResolver(music, contentResolver);
                }
                // for android 10 we must catch a recoverable security exception
                catch (RecoverableSecurityException e) {
                    final IntentSender intent = e.getUserAction().getActionIntent().getIntentSender();
                    // Create an IntentSenderRequest for the delete request
                    IntentSenderRequest request = new IntentSenderRequest.Builder(intent).build();
                    // Launch the delete request using the ActivityResultLauncher
                    deleteMusicRequestLauncher.launch(request);
                }
            }
        } else {
            // for older devices
            for (Music music : musics) {
                try {
                    deleteItemWithContentResolver(music, contentResolver);
                } catch (Exception e) {
                    showToast("Failed to delete this song");
                }
            }
        }
    }

    private void deleteItemWithContentResolver(@NonNull Music music, ContentResolver contentResolver) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View customLayout = getLayoutInflater().inflate(R.layout.delete_layout, null);
        builder.setView(customLayout);
        builder.setCancelable(false);
        ImageView imageView = customLayout.findViewById(R.id.cover_image);
        glideBuilt.glide(music.getAlbumUri(), R.drawable.ic_music_thumbnail, imageView, 512);
        builder.setPositiveButton("Allow", (dialog, i) -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestWriteStoragePermissionBelow11();
            } else {
                Uri contentUri = getContentUri(music);
                String[] selectionArgs = new String[]{music.getId()};

                if (contentUri != null) {
                    contentResolver.delete(contentUri, MediaStore.Audio.Media._ID + "=?", selectionArgs);
                    updateAdaptersForRemovedItem();
                }
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Deny", null);
        AlertDialog dialog;
        dialog = builder.create();
        dialog.show();
    }

    public void updateAdaptersForRemovedItem() {
        musicMainAdapter.removeItem(selectedItem);
        bottomSheetPlayerFragment.queueAdapter.removeItem(selectedItem);
        if (lastAddedFragment != null) {
            lastAddedFragment.adapter.removeItem(selectedItem);
        }
        if (searchFragment != null && searchFragment.adapter != null) {
            searchFragment.adapter.removeItem(selectedItem);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addToNextPlay(Music music) {
        bottomSheetPlayerFragment.queueAdapter.updateItemInserted(music);
    }

    private void addToQueue(Music music) {
        bottomSheetPlayerFragment.queueAdapter.updateItemInsertedLast(music);
    }

    private void closeOptionSheet() {
        optionSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            phoneStateCallback = new CustomCallStateListener();

            telephonyManager.registerTelephonyCallback(ContextCompat.getMainExecutor(this), phoneStateCallback);
        } else {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    super.onCallStateChanged(state, phoneNumber);
                    takeActionOnCall(state);
                }
            };
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private class CustomCallStateListener extends PhoneStateCallback {
        @Override
        public void onCallStateChanged(int state) {
            takeActionOnCall(state);
        }
    }

    private void takeActionOnCall(int state) {
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

    //Checks whether user granted permissions for external storage or not
    //if not then shows dialogue to grant permissions
    private void requestReadStorageAndPhonePermissionBelow12() {
        Dexter.withContext(MainActivity.this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        requestForegroundServicePermissionAbove8();
                    }
                    is_granted = true;
                    setUpServiceAndScanner();
                } else {
                    is_granted = false;
                    showToast("Permissions denied!");
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                is_granted = false;
                permissionToken.continuePermissionRequest();
            }
        }).check();

    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestReadAudioAndPhonePermissionAbove12() {
        Dexter.withContext(MainActivity.this).withPermissions(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_PHONE_STATE).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                    requestForegroundServicePermissionAbove8();
                    is_granted = true;
                    setUpServiceAndScanner();
                } else {
                    is_granted = false;
                    showToast("Permissions denied!");
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                is_granted = false;
                permissionToken.continuePermissionRequest();
            }
        }).check();

    }

    private void requestWriteStoragePermissionBelow11() {
        Dexter.withContext(MainActivity.this).withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                showToast("Permission denied!");
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void requestForegroundServicePermissionAbove8() {
        Dexter.withContext(MainActivity.this).withPermission(Manifest.permission.FOREGROUND_SERVICE).withListener(new PermissionListener() {
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

    private void setUpServiceAndScanner() {
        setUpMusicScanner();
        callStateListener();
        bindService();
        startService();
    }

    public void checkForUpdateList(boolean enableLoading) {
        // do in background code here
        ExecutorService service = Executors.newFixedThreadPool(1);
        if (enableLoading) {
            setObserver();
        }
        isChecking = true;
        service.execute(() -> musicUtils.fetchMusic(this).thenAccept(it -> {
            setMusicAdapter(musicUtils.getInitialMusicList());
            isChecking = false;
        }).exceptionally(it -> {
            showToast(it.getMessage());
            isChecking = false;
            return null;
        }));
        service.shutdown();
    }

    private void setObserver() {
        musicUtils.getStatus().observe(this, it -> {
            switch (it) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    linearLayout.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    ArrayList<Music> list = musicUtils.getInitialMusicList();
                    storageUtil.saveInitialList(list);
                    if (list.isEmpty()) {
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                    progressBar.setVisibility(View.GONE);
                    break;
                case FAILURE:
                    linearLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        });
    }

    /**
     * checks if music list already available in local app storage otherwise scans media storage for music
     */
    private void setUpMusicScanner() {
        //Fetch Music List along with it's metadata and save it in "dataList"
        ArrayList<Music> loadList = storageUtil.loadInitialList();
        if (loadList != null && !loadList.isEmpty()) {
            setMusicAdapter(loadList);
        } else {
            if (!isChecking) {
                checkForUpdateList(true);
            }
        }

    }

    private void setMusicAdapter(ArrayList<Music> musicArrayList) {
        runOnUiThread(() -> {
            if (musicMainAdapter == null) {
                LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
                musicMainAdapter = new MusicMainAdapter(MainActivity.this, musicArrayList);
                musicRecyclerView.setHasFixedSize(true);
                musicRecyclerView.setLayoutManager(manager);
                musicRecyclerView.setAdapter(musicMainAdapter);
            } else {
                musicMainAdapter.updateMusicListItems(musicArrayList);
                storageUtil.saveInitialList(musicArrayList);
            }
        });
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment1 = fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG);
        Fragment fragment2 = fragmentManager.findFragmentByTag(PLAYLISTS_FRAGMENT_TAG);
        Fragment fragment3 = fragmentManager.findFragmentByTag(ABOUT_FRAGMENT_TAG);
        Fragment fragment4 = fragmentManager.findFragmentByTag(LAST_ADDED_FRAGMENT_TAG);
        Fragment fragment5 = fragmentManager.findFragmentByTag(FAVORITE_FRAGMENT_TAG);
        Fragment fragment6 = fragmentManager.findFragmentByTag(TAG_EDITOR_FRAGMENT_TAG);
        Fragment fragment7 = fragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG);
        Fragment fragment8 = fragmentManager.findFragmentByTag(ADD_LYRICS_FRAGMENT_TAG);
        Fragment fragment9 = fragmentManager.findFragmentByTag(OPEN_PLAYLIST_FRAGMENT_TAG);

        if (mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (lyricsListBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || lyricsListBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    lyricsListBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else {
                    if (optionSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || optionSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        optionSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    } else {
                        if (fragment1 != null || fragment2 != null || fragment3 != null || fragment4 != null) {
                            navigationView.setCheckedItem(R.id.navigation_home);
                        }
                        if (is_playing){
                            if (fragment1 != null ||fragment2 != null ||fragment3 != null ||fragment4 != null ||fragment5 != null||fragment6 != null||fragment7 != null||fragment8 != null||fragment9 != null) {
                                fragmentManager.popBackStackImmediate();
                            }else {
                                moveTaskToBack(false);
                            }
                        }else {
                            super.onBackPressed();
                        }
                    }
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
        Fragment fragment1 = fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG);
        Fragment fragment2 = fragmentManager.findFragmentByTag(PLAYLISTS_FRAGMENT_TAG);
        Fragment fragment3 = fragmentManager.findFragmentByTag(ABOUT_FRAGMENT_TAG);
        Fragment fragment4 = fragmentManager.findFragmentByTag(FAVORITE_FRAGMENT_TAG);
        Fragment fragment5 = fragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG);
        Fragment fragment6 = fragmentManager.findFragmentByTag(LAST_ADDED_FRAGMENT_TAG);

        switch (item.getItemId()) {
            case R.id.navigation_home: {
                if (fragment1 != null || fragment2 != null || fragment3 != null || fragment6 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                break;
            }
            case R.id.navigation_setting: {
                if (fragment2 != null || fragment3 != null || fragment5 != null || fragment6 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                if (fragment1 == null) {
                    replaceFragment(R.id.sec_container, new SettingsFragment(), android.R.transition.slide_right, SETTINGS_FRAGMENT_TAG);
                }
                break;
            }
            case R.id.navigation_playlist: {
                if (fragment1 != null || fragment3 != null || fragment4 != null || fragment5 != null || fragment6 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                if (fragment2 == null) {
                    replaceFragment(R.id.sec_container, new PlaylistsFragment(), android.R.transition.slide_right, PLAYLISTS_FRAGMENT_TAG);
                }
                break;
            }
            case R.id.navigation_about: {
                if (fragment1 != null || fragment2 != null || fragment5 != null || fragment6 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                if (fragment3 == null) {
                    replaceFragment(R.id.sec_container, new AboutFragment(), android.R.transition.slide_right, ABOUT_FRAGMENT_TAG);
                }
                break;
            }
            case R.id.navigation_last_added: {
                if (fragment1 != null || fragment2 != null || fragment5 != null || fragment3 != null) {
                    fragmentManager.popBackStackImmediate();
                }
                if (fragment6 == null) {
                    replaceFragment(R.id.sec_container, new LastAddedFragment(), android.R.transition.slide_right, LAST_ADDED_FRAGMENT_TAG);
                }
                break;
            }
            case R.id.navigation_donate: {
                donationSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            }
        }

        return true;
    }

    /**
     * Fragment replacer
     *
     * @param container the layout id you want to replace with
     * @param fragment  new Fragment you want to replace
     * @param animation ex- android.R.transition.explode
     * @param tag       fragment tag to call it later
     */
    private void replaceFragment(int container, Fragment fragment, int animation, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(animation));
        transaction.replace(container, fragment, tag).addToBackStack(null).commit();
    }

    public void openBottomSheet(Bundle bundle) {
        setLyricListAdapter(bundle);
        lyricsListBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        closeOptionSheet();
        closePlOptionSheet();
        switch (v.getId()) {
            case R.id.add_play_next_option: {
                addToNextPlay(selectedItem);
                break;
            }
            case R.id.add_to_queue_option: {
                addToQueue(selectedItem);
                break;
            }
            case R.id.add_to_playlist_option: {
                addToPlaylist(selectedItem);
                break;
            }
            case R.id.set_ringtone_option: {
                setRingtone(selectedItem);
                break;
            }
            case R.id.delete_music_option: {
                List<Music> list = new ArrayList<>();
                list.add(selectedItem);
                deleteFromDevice(list);
                break;
            }
            case R.id.tagEditor_option: {
                openTagEditor(selectedItem);
                break;
            }
            case R.id.addLyrics_option: {
                bottomSheetPlayerFragment.setLyricsLayout(selectedItem);
                break;
            }
            case R.id.details_option: {
                openDetailsBox(selectedItem);
                break;
            }
            case R.id.share_music_option: {
                openShare(selectedItem);
                break;
            }
            case R.id.add_to_favourites_option: {
                bottomSheetPlayerFragment.addFavorite(storageUtil, selectedItem, addToFav);
                break;
            }
            case R.id.add_play_next_pl_option: {
                addToNextPlayPl(plItemSelected);
                break;
            }
            case R.id.add_to_queue_pl_option: {
                addToQueuePl(plItemSelected);
                break;
            }
            case R.id.rename_pl_option: {
                openRenameDialog(plItemSelected);
                break;
            }
            case R.id.choose_cover_option: {
                changeUriPl(plItemSelected);
                break;
            }
            case R.id.delete_pl_option: {
                deletePl(plItemSelected);
                break;
            }
            case R.id.remove_music_option: {
                removeFromList(selectedItem, optionTag);
                break;
            }
        }

    }

    private void removeFromList(Music music, OptionSheetEnum optionTag) {
        if (optionTag == OPEN_PLAYLIST) {
            EventBus.getDefault().post(new RemoveFromPlaylistEvent(music));
            //solve removed song not loading in playlist adapter
        } else if (optionTag == FAVORITE_LIST) {
            EventBus.getDefault().post(new RemoveFromFavoriteEvent(music));
        }
    }

    private void openRenameDialog(Playlist playlist) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View customLayout = getLayoutInflater().inflate(R.layout.rename_playlist_layout, null);
        builder.setView(customLayout);
        builder.setCancelable(true);

        playlistArrayList = storageUtil.getAllPlaylist();
        EditText editText = customLayout.findViewById(R.id.edit_playlist_rename);

        editText.setText(playlist.getName());

        builder.setPositiveButton("OK", (dialog, i) -> {
            String plKey = editText.getText().toString().trim();

            ArrayList<String> playlistNames = new ArrayList<>();
            for (Playlist playlist1 : playlistArrayList) {
                playlistNames.add(playlist1.getName());
            }

            if (!plKey.equals("")) {
                if (!playlistNames.contains(plKey)) {
                    renamePl(playlist, plKey);
                    dialog.dismiss();
                } else {
                    editText.setError("Name already exist");
                }
            } else {
                editText.setError("Empty");
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog renameDialog = builder.create();
        renameDialog.show();
    }

    private void deletePl(Playlist playlist) {
        playlistFragment.playlistList.remove(playlist);
        storageUtil.removePlayList(playlist.getName());
        ArrayList<Playlist> arrayList = storageUtil.getAllPlaylist();

        playlistFragment.playlistAdapter.updateView(arrayList);
    }

    private void renamePl(Playlist playlist, String newName) {
        storageUtil.replacePlaylist(playlist, newName, playlist.getCoverUri());
        ArrayList<Playlist> arrayList = storageUtil.getAllPlaylist();

        playlistFragment.playlistAdapter.updateView(arrayList);
    }

    private void changeUriPl(Playlist playlist) {
        pl_name = playlist.getName();
        pickImage(pickIntentForPLCoverChange, mediaPickerForPLCoverChange);
    }

    private void addToQueuePl(Playlist playlist) {
        ArrayList<Music> list = playlist.getMusicList();
        bottomSheetPlayerFragment.queueAdapter.updateListInsertedLast(list);
    }

    private void addToNextPlayPl(Playlist playlist) {
        ArrayList<Music> list = playlist.getMusicList();
        bottomSheetPlayerFragment.queueAdapter.updateListInserted(list);
    }

    private void closePlOptionSheet() {
        plSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void addToPlaylist(Music music) {
        openPlaylistDialog(music);
    }

    private TextView noPl_tv;

    private void openPlaylistDialog(Music music) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View customLayout = getLayoutInflater().inflate(R.layout.playlist_dialog_layout, null);
        builder.setView(customLayout);
        builder.setCancelable(true);
        //Initialize Dialogue Box UI Items
        playlistArrayList = storageUtil.getAllPlaylist();
        ImageView image = customLayout.findViewById(R.id.add_to_fav_dialog_box_img);
        if (!storageUtil.checkFavourite(music)) {
            image.setImageResource(R.drawable.ic_favorite_border);
        } else if (storageUtil.checkFavourite(music)) {
            image.setImageResource(R.drawable.ic_favorite);
        }
        plDialogRecyclerView = customLayout.findViewById(R.id.playlist_dialog_recycler);
        View createPlaylistBtnDialog = customLayout.findViewById(R.id.create_playlist);
        View addFavBtnDialog = customLayout.findViewById(R.id.add_to_fav_dialog_box);
        noPl_tv = customLayout.findViewById(R.id.text_no_pl);

        createPlaylistBtnDialog.setOnClickListener(v -> openCreatePlaylistDialog(music));

        addFavBtnDialog.setOnClickListener(v -> {
            addToFavorite(music);
            addToPlDialog.dismiss();
        });

        LinearLayoutManager manager = new LinearLayoutManagerWrapper(this);
        plDialogRecyclerView.setLayoutManager(manager);

        if (playlistArrayList != null) {
            playlistDialogAdapter = new PlaylistDialogAdapter(this, playlistArrayList, music);
            if (playlistArrayList.isEmpty()) {
                noPl_tv.setVisibility(View.VISIBLE);
            } else {
                noPl_tv.setVisibility(View.GONE);
            }

            plDialogRecyclerView.setAdapter(playlistDialogAdapter);
        } else {
            noPl_tv.setVisibility(View.VISIBLE);
        }
        addToPlDialog = builder.create();
        addToPlDialog.show();
    }

    private void addToFavorite(Music music) {
        storageUtil.saveFavorite(music);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openCreatePlaylistDialog(Music music) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View customLayout = getLayoutInflater().inflate(R.layout.create_playlist_layout, null);
        builder.setView(customLayout);
        builder.setCancelable(true);

        EditText editText = customLayout.findViewById(R.id.edit_playlist_name);
        playlist_image_View = customLayout.findViewById(R.id.playlist_image_view);
        View playerPickCover_l = customLayout.findViewById(R.id.playlist_cover_pick);

        playerPickCover_l.setOnClickListener(v -> pickImage(pickIntentForPLCover, mediaPickerForPLCover));

        //check if previous list contains the same name as we are saving
        builder.setPositiveButton("OK", (dialog, i) -> {
            String plKey = editText.getText().toString().trim();
            String plCoverUri = playListImageUri != null ? playListImageUri.toString() : "";

            ArrayList<String> playlistNames = new ArrayList<>();
            for (Playlist playlist : playlistArrayList) {
                playlistNames.add(playlist.getName());
            }

            if (!plKey.equals("")) {
                if (!playlistNames.contains(plKey)) {
                    storageUtil.createPlaylist(plKey, plCoverUri);
                    ArrayList<Playlist> allList = storageUtil.getAllPlaylist();
                    if (playlistArrayList != null) {
                        playlistArrayList.clear();
                        playlistArrayList.addAll(allList);
                        if (noPl_tv != null && noPl_tv.getVisibility() == View.VISIBLE) {
                            noPl_tv.setVisibility(View.GONE);
                        }
                        playlistDialogAdapter.notifyDataSetChanged();
                        playListImageUri = null;
                    } else {
                        playlistDialogAdapter = new PlaylistDialogAdapter(this, allList, music);
                        plDialogRecyclerView.setAdapter(playlistDialogAdapter);
                    }
                    dialog.dismiss();
                } else {
                    editText.setError("Name already exist");
                }
            } else {
                editText.setError("Empty");
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog plDialog = builder.create();
        plDialog.show();
    }


    private void openShare(Music music) {
        Uri uri = Uri.parse(music.getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share Via ..."));
    }


    private void openDetailsBox(Music music) {
        detailsSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        int bitrateInNum = 0;
        String bitrate = music.getBitrate();
        if (!TextUtils.isEmpty(bitrate)) {
            bitrateInNum = Integer.parseInt(bitrate) / 1000;
        }
        String finalBitrate = bitrateInNum + " KBPS";


        songPathTv.setText(music.getPath());
        songNameTv.setText(music.getName());
        songArtistTv.setText(music.getArtist());
        songSizeTv.setText(convertByteSizeToReadableSize(music.getSize()));
        songAlbumTv.setText(music.getAlbum());

        String genre = music.getGenre();
        if (TextUtils.isEmpty(genre)) {
            genre = "Unknown";
        }
        songGenreTv.setText(genre);
        songBitrateTv.setText(finalBitrate);

    }

    private String convertByteSizeToReadableSize(String fileSize) {
        float size = Float.parseFloat(fileSize) / (1024 * 1024);

        int pos = String.valueOf(size).indexOf(".");

        String beforeDot = String.valueOf(size).substring(0, pos);
        String afterDot;
        try {
            afterDot = String.valueOf(size).substring(pos, pos + 3);
        } catch (IndexOutOfBoundsException e) {
            afterDot = "0";
        }

        String sizeExt = " mb";
        int beforeDotInt = Integer.parseInt(beforeDot);
        if (beforeDotInt % 1024 != beforeDotInt) {
            float in = Float.parseFloat(beforeDot);
            size = in / 1024;

            pos = String.valueOf(size).indexOf(".");
            beforeDot = String.valueOf(size).substring(0, pos);
            try {
                afterDot = String.valueOf(size).substring(pos, pos + 3);
            } catch (IndexOutOfBoundsException e) {
                afterDot = "0";
            }
            sizeExt = " gb";
        }
        return beforeDot + afterDot + sizeExt;
    }

    private void openTagEditor(Music itemSelected) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment1 = fragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG);
        Fragment fragment2 = fragmentManager.findFragmentByTag(TAG_EDITOR_FRAGMENT_TAG);

        if (fragment1 != null) {
            fragmentManager.popBackStackImmediate();
        }
        if (mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        String encodedMessage = MusicHelper.encode(itemSelected);


        if (fragment2 == null) {
            TagEditorFragment fragment = new TagEditorFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("currentMusic", encodedMessage);
            fragment.setArguments(bundle);
            replaceFragment(R.id.sec_container, fragment, android.R.transition.no_transition, TAG_EDITOR_FRAGMENT_TAG);
        }
    }

    /**
     * get Content uri of music (Required to delete files in android 11 and above)
     *
     * @param music music
     * @return returns content uri of given music
     */
    private Uri getContentUri(Music music) {
        int id = Integer.parseInt(music.getId());
        Uri baseUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return Uri.withAppendedPath(baseUri, "" + id);

    }
}