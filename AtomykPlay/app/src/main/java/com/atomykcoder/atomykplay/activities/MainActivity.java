package com.atomykcoder.atomykplay.activities;

import static com.atomykcoder.atomykplay.enums.OptionSheetEnum.FAVORITE_LIST;
import static com.atomykcoder.atomykplay.enums.OptionSheetEnum.OPEN_PLAYLIST;
import static com.atomykcoder.atomykplay.helperFunctions.CustomMethods.pickImage;
import static com.atomykcoder.atomykplay.services.MediaPlayerService.is_playing;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.atomykcoder.atomykplay.classes.ApplicationClass;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.customScripts.CustomBottomSheet;
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
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

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
    private static final int DELETE_ITEM = 7461;
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
    public BottomSheetBehavior<View> lyricsListBehavior;
    public BottomSheetBehavior<View> optionSheetBehavior;
    public BottomSheetBehavior<View> donationSheetBehavior;
    public BottomSheetBehavior<View> detailsSheetBehavior;
    public BottomSheetBehavior<View> plSheetBehavior;
    public Dialog addToPlDialog;
    public View plSheet, player_bottom_sheet;
    public Playlist plItemSelected;
    public Music selectedItem;
    public boolean isChecking = false;
    private Dialog plDialog;
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
    private final BottomSheetBehavior.BottomSheetCallback detailsSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                shadowOuterSheet2.setClickable(true);
                shadowOuterSheet2.setFocusable(true);
                shadowOuterSheet2.setAlpha(0.45f);
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                shadowOuterSheet2.setClickable(true);
                shadowOuterSheet2.setFocusable(true);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                shadowOuterSheet2.setAlpha(1f);
            } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                shadowOuterSheet2.setClickable(false);
                shadowOuterSheet2.setFocusable(false);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            shadowOuterSheet2.setAlpha(0.45f + slideOffset);
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

    private final BottomSheetBehavior.BottomSheetCallback plSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
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
    private int tempThemeColor;
    private TextView navSongName, navArtistName;
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
                changeNavigationColor(tempThemeColor, getResources().getColor(R.color.player_bg, null));
                tempThemeColor = getResources().getColor(R.color.player_bg, null);
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
                bottomSheetPlayerFragment.getThemeColor().observe(MainActivity.this, it -> {
                    changeNavigationColor(tempThemeColor, it);
                    tempThemeColor = it;
                });
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
    private Dialog renameDialog;
    private AlertDialog ringtoneDialog = null;
    private String pl_name;
    private OptionSheetEnum optionTag;
    private TextView songPathTv, songNameTv, songArtistTv, songSizeTv, songGenreTv, songBitrateTv, songAlbumTv;
    private Handler handler;
    private MusicUtils musicUtils;
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
            playlistFragment.playlistAdapter.updateView(storageUtil.getAllPlaylist());

        }
    });

    private final ActivityResultLauncher<Intent> pickIntentForPLCoverChange = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                String coverImageUri = result.getData().getData().toString();
                storageUtil.replacePlaylist(storageUtil.loadPlaylist(pl_name), pl_name, coverImageUri);
                playlistFragment.playlistAdapter.updateView(storageUtil.getAllPlaylist());

            }
        }
    });

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
        mainPlayerSheetBehavior.addBottomSheetCallback(bottomSheetCallback);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.navigation_home);
        }
    }

    private void checkForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            is_granted = false;
            showRequestDialog();
        } else {
            is_granted = true;
            setUpServiceAndScanner();
        }
    }

    private void showRequestDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View customLayout = getLayoutInflater().inflate(R.layout.permission_request_dialog_layout, null);
        builder.setView(customLayout);
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            requestPermission();
        });
        builder.setNegativeButton("Cancel", null);
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
        if (is_granted) {
            if (!isChecking) {
                checkForUpdateList(false);
            }
        }
        if (service_bound) {
            if (is_playing) {
                media_player_service.setSeekBar();
                EventBus.getDefault().post(new PrepareRunnableEvent());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        colorAnimation.setDuration(200);
        colorAnimation.addUpdateListener(animator -> window.setNavigationBarColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        if (service_bound) {
            MainActivity.this.unbindService(service_connection);
            //if media player is not playing it will stop the service
            if (!is_playing) {
                stopService(new Intent(MainActivity.this, MediaPlayerService.class));
                service_stopped = true;
            }
        }
        MediaPlayerService.ui_visible = false;
        if (phoneStateListener != null && telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        playlistFragment = null;
    }

    /**
     * this function starts service and binds to MainActivity
     */
    private void bindService() {
        Intent playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
        MainActivity.this.bindService(playerIntent, service_connection, Context.BIND_AUTO_CREATE);
    }

    private void startService() {
        Intent playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
        startService(playerIntent);
        service_stopped = false;
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
        Fragment fragment3 = fragmentManager.findFragmentByTag(LAST_ADDED_FRAGMENT_TAG);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        LastAddedFragment lastAddedFragment = new LastAddedFragment();
        if (fragment3 == null) {
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
        View lyricsListView = findViewById(R.id.found_lyrics_fragments);
        lyricsListBehavior = BottomSheetBehavior.from(lyricsListView);

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
        glideBuilt.glide(null, R.drawable.ic_music, navCover, 300);
    }

    public void setDataInNavigation(String song_name, String artist_name, Bitmap album_uri) {
        navSongName.setText(song_name);
        navArtistName.setText(artist_name);
        try {
            glideBuilt.glideBitmap(album_uri, R.drawable.ic_music, navCover, 300);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        FoundLyricsAdapter adapter = new FoundLyricsAdapter(titles, sampleLyrics, urls, this);
        recyclerView.setAdapter(adapter);
    }

    public void stopMusic() {
        if (!service_stopped) {
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

    /**
     * play from start
     */
    public void playAudio(Music music) {
        //starting service if its not started yet otherwise it will send broadcast msg to service
        storageUtil.clearMusicLastPos();

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
        if (MusicUtils.getInstance().shouldChangeShuffleMode()) {
            storageUtil.saveShuffle(true);
        }
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
    }

    /**
     * next music
     */
    public void playNextAudio() {
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
    }

    /**
     * previous music
     */
    public void playPreviousAudio() {
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
        replaceFragment(R.id.sec_container, new SearchFragment(), android.R.transition.fade, SEARCH_FRAGMENT_TAG);
    }

    private void setRingtone(Music music) {

        boolean canWrite = Settings.System.canWrite(this);

        if (canWrite) {
            Uri newUri = Uri.fromFile(new File(music.getPath()));
            AlertDialog.Builder ringtoneDialog = new AlertDialog.Builder(MainActivity.this);
            ringtoneDialog.setTitle("Confirmation");
            ringtoneDialog.setMessage(music.getName() + " - Set as ringtone");
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
                glideBuilt.glideBitmap(finalImage, R.drawable.ic_music, optionCover, 65);
            });
        });

    }

    public void openPlOptionMenu(Playlist currentItem) {
        plItemSelected = currentItem;
        String count = currentItem.getMusicList().size() + " Songs";
        plSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        plOptionName.setText(currentItem.getName());
        optionPlCount.setText(count);
        glideBuilt.glide(currentItem.getCoverUri(), R.drawable.ic_music_list, plOptionCover, 200);
    }

    /**
     * delete song permanently from device *USE WITH CAUTION*
     *
     * @param music music to be deleted
     */
    private void deleteFromDevice(Music music) {
        Uri contentUri = getContentUri(music);
        ContentResolver contentResolver = getContentResolver();
        Uri normalUri = Uri.fromFile(new File(music.getId()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // for android 11 and above
            if (contentUri != null) {
                List<Uri> uris = new ArrayList<>(1);
                uris.add(contentUri);
                PendingIntent pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris);
                try {
                    //noinspection deprecation
                    startIntentSenderForResult(pendingIntent.getIntentSender(), DELETE_ITEM, null, 0, 0, 0, null);
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
                        startIntentSenderForResult(intent, DELETE_ITEM, null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } else {
            // for older devices
            if (normalUri != null) {
                contentResolver.delete(normalUri, null, null);
                bottomSheetPlayerFragment.queueAdapter.removeItem(selectedItem);
                musicMainAdapter.removeItem(selectedItem);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CANCELED) {
            return;
        }

        switch (requestCode) {

            // check for delete item request
            case DELETE_ITEM:
                musicMainAdapter.removeItem(selectedItem);
                bottomSheetPlayerFragment.queueAdapter.removeItem(selectedItem);
                break;
            // check for blacklist folder select request
            case TAG_BLOCK_LIST:
                if (data != null) {
                    // find fragment
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    SettingsFragment fragment = (SettingsFragment) fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG);
                    // set settings fragment UI here
                    if (fragment != null) {
                        Uri uri = data.getData();
                        String treePath = uri.getPath();
                        String pathUri = convertTreeUriToPathUri(treePath);
                        //save path in blacklist storage
                        StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(this);
                        settingsStorage.saveInBlackList(pathUri);
                        checkForUpdateList(true);
                    }
                }
                break;
        }
    }

    /**
     * converts tree path uri to usable path uri (WORKS WITH BOTH INTERNAL AND EXTERNAL STORAGE)
     *
     * @param treePath tree path to be converted
     * @return returns usable path uri
     */
    private String convertTreeUriToPathUri(String treePath) {
        if (treePath.contains("/tree/primary:")) {
            treePath = treePath.replace("/tree/primary:", "/storage/emulated/0/");
        } else {
            int colonIndex = treePath.indexOf(":");
            String sdCard = treePath.substring(6, colonIndex);
            String folders = treePath.substring(colonIndex + 1);
            treePath = "/storage/" + sdCard + "/" + folders;
        }
        return treePath;
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
    private void requestPermission() {
        Dexter.withContext(MainActivity.this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                    is_granted = true;
                    setUpServiceAndScanner();
                } else {
                    is_granted = false;
                    ((ApplicationClass) getApplication()).showToast("Permissions denied!");
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                is_granted = false;
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
            ((ApplicationClass) getApplication()).showToast(it.getMessage());
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
                    storageUtil.saveInitialList(musicUtils.getInitialMusicList());
                    try {
                        if (musicUtils.getInitialMusicList().isEmpty()) {
                            linearLayout.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
            checkForUpdateList(true);
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

    @SuppressLint("SwitchIntDef")
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment1 = fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG);
        Fragment fragment2 = fragmentManager.findFragmentByTag(PLAYLISTS_FRAGMENT_TAG);
        Fragment fragment3 = fragmentManager.findFragmentByTag(ABOUT_FRAGMENT_TAG);
        Fragment fragment4 = fragmentManager.findFragmentByTag(LAST_ADDED_FRAGMENT_TAG);

        if (mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED ||
                mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (lyricsListBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED ||
                        lyricsListBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    lyricsListBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else {
                    if (optionSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED ||
                            optionSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        optionSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    } else {
                        if (fragment1 != null || fragment2 != null || fragment3 != null || fragment4 != null) {
                            navigationView.setCheckedItem(R.id.navigation_home);
                        }
                        super.onBackPressed();
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
                deleteFromDevice(selectedItem);
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
        renameDialog = new Dialog(MainActivity.this);

        renameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        renameDialog.setContentView(R.layout.rename_playlist_layout);

        playlistArrayList = storageUtil.getAllPlaylist();
        EditText editText = renameDialog.findViewById(R.id.edit_playlist_rename);
        Button btnOk = renameDialog.findViewById(R.id.btn_ok_rename_pl);
        Button btnCancel = renameDialog.findViewById(R.id.btn_cancel_rename_pl);

        editText.setText(playlist.getName());

        btnOk.setOnClickListener(v -> {
            String plKey = editText.getText().toString().trim();

            ArrayList<String> playlistNames = new ArrayList<>();
            for (Playlist playlist1 : playlistArrayList) {
                playlistNames.add(playlist1.getName());
            }

            if (!plKey.equals("")) {
                if (!playlistNames.contains(plKey)) {
                    renamePl(playlist, plKey);
                    renameDialog.dismiss();
                } else {
                    editText.setError("Name already exist");
                }
            } else {
                editText.setError("Empty");
            }
        });
        btnCancel.setOnClickListener(v -> renameDialog.dismiss());
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
        addToPlDialog = new Dialog(MainActivity.this);

        addToPlDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addToPlDialog.setContentView(R.layout.playlist_dialog_layout);

        //Initialize Dialogue Box UI Items
        playlistArrayList = storageUtil.getAllPlaylist();
        ImageView image = addToPlDialog.findViewById(R.id.add_to_fav_dialog_box_img);
        if (!storageUtil.checkFavourite(music)) {
            image.setImageResource(R.drawable.ic_favorite_border);
        } else if (storageUtil.checkFavourite(music)) {
            image.setImageResource(R.drawable.ic_favorite);
        }
        plDialogRecyclerView = addToPlDialog.findViewById(R.id.playlist_dialog_recycler);
        View createPlaylistBtnDialog = addToPlDialog.findViewById(R.id.create_playlist);
        View addFavBtnDialog = addToPlDialog.findViewById(R.id.add_to_fav_dialog_box);
        noPl_tv = addToPlDialog.findViewById(R.id.text_no_pl);

        createPlaylistBtnDialog.setOnClickListener(v -> openCreatePlaylistDialog(music));

        addFavBtnDialog.setOnClickListener(v -> {
            addToFavorite(music);
            addToPlDialog.dismiss();
        });

        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
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
        addToPlDialog.show();
    }

    private void addToFavorite(Music music) {
        storageUtil.saveFavorite(music);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openCreatePlaylistDialog(Music music) {
        plDialog = new Dialog(MainActivity.this);
        plDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        plDialog.setContentView(R.layout.create_playlist_layout);

        EditText editText = plDialog.findViewById(R.id.edit_playlist_name);
        Button btnOk = plDialog.findViewById(R.id.btn_ok_pl);
        Button btnCancel = plDialog.findViewById(R.id.btn_cancel_pl);
        playlist_image_View = plDialog.findViewById(R.id.playlist_image_view);
        View playerPickCover_l = plDialog.findViewById(R.id.playlist_cover_pick);

        playerPickCover_l.setOnClickListener(v -> pickImage(pickIntentForPLCover, mediaPickerForPLCover));

        //check if previous list contains the same name as we are saving
        btnOk.setOnClickListener(v -> {
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
                    plDialog.dismiss();
                } else {
                    editText.setError("Name already exist");
                }
            } else {
                editText.setError("Empty");
            }
        });
        btnCancel.setOnClickListener(v -> plDialog.dismiss());
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

        int bitrateInNum = Integer.parseInt(music.getBitrate()) / 1000;

        float size = Float.parseFloat(music.getSize()) / (1024 * 1024);

        int pos = String.valueOf(size).indexOf(".");

        String beforeDot = String.valueOf(size).substring(0, pos);
        String afterDot = String.valueOf(size).substring(pos, pos + 3);

        String finalBitrate = bitrateInNum + " KBPS";
        String finalSize = beforeDot + afterDot + " mb";

        songPathTv.setText(music.getPath());
        songNameTv.setText(music.getName());
        songArtistTv.setText(music.getArtist());
        songSizeTv.setText(finalSize);
        songBitrateTv.setText(finalBitrate);
        songAlbumTv.setText(music.getAlbum());
        songGenreTv.setText(music.getGenre());

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
        Uri baseUri = Uri.parse("content://media/external/audio/media");
        return Uri.withAppendedPath(baseUri, "" + id);

    }
}