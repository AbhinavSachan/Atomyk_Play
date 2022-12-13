package com.atomykcoder.atomykplay.fragments;

import static com.atomykcoder.atomykplay.activities.MainActivity.media_player_service;
import static com.atomykcoder.atomykplay.activities.MainActivity.service_bound;
import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;
import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertToMillis;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.favorite;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.no_favorite;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.no_repeat;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.no_shuffle;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.repeat;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.repeat_one;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.shuffle;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.MusicLyricsAdapter;
import com.atomykcoder.atomykplay.adapters.MusicQueueAdapter;
import com.atomykcoder.atomykplay.adapters.SimpleTouchCallback;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.customScripts.CenterSmoothScrollScript;
import com.atomykcoder.atomykplay.customScripts.CustomBottomSheet;
import com.atomykcoder.atomykplay.enums.PlaybackStatus;
import com.atomykcoder.atomykplay.events.PrepareRunnableEvent;
import com.atomykcoder.atomykplay.events.RemoveLyricsHandlerEvent;
import com.atomykcoder.atomykplay.events.RunnableSyncLyricsEvent;
import com.atomykcoder.atomykplay.events.SetMainLayoutEvent;
import com.atomykcoder.atomykplay.events.UpdateMusicImageEvent;
import com.atomykcoder.atomykplay.events.UpdateMusicProgressEvent;
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.viewModals.LRCMap;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BottomSheetPlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, OnDragStartListener {

    private final ArrayList<String> lyricsArrayList = new ArrayList<>();
    private final CountDownTimer[] countDownTimer = new CountDownTimer[1];
    public CustomBottomSheet<View> queueSheetBehaviour;
    public Runnable lyricsRunnable;
    public Handler lyricsHandler;
    public LinearProgressIndicator mini_progress;
    public ImageView mini_pause;
    //main player seekbar
    public SeekBar seekBarMain;
    public ImageView playImg;
    public TextView curPosTv;
    public ImageView mini_cover, mini_next;
    public TextView mini_name_text, mini_artist_text;
    public View mini_play_view;
    public View player_layout;
    public ImageView optionImg;
    public View info_layout;
    public MusicQueueAdapter queueAdapter;
    private boolean userScrolling = false;
    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (newState == 0) {
                userScrolling = false;
            } else if (newState == 1 || newState == 2) {
                userScrolling = true;
            }
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };
    private Context context;
    private TextView durationTv;
    private ImageView playerCoverImage;
    private ImageView favoriteImg;
    private TextView playerSongNameTv, playerArtistNameTv, mimeTv, bitrateTv, timerTv;
    private View noLyricsLayout;
    //cover image view
    private RecyclerView lyricsRecyclerView;
    private LRCMap lrcMap;
    private RecyclerView.LayoutManager lm;
    private String songName, artistName, mimeType, duration, bitrate, albumUri;
    private View queueBottomSheet;
    private ImageView repeatImg;
    private ImageView shuffleImg;
    private ImageView timerImg;
    //setting up mini player layout
    //calling it from service when player is prepared and also calling it in this fragment class
    //to set it on app start ☺
    private StorageUtil storageUtil;
    private ItemTouchHelper itemTouchHelper;
    private RecyclerView queueRecyclerView;
    private View lyricsRelativeLayout;
    private MainActivity mainActivity;
    private CardView coverCardView;
    private ImageView lyricsImg, queueCoverImg;
    private View shadowPlayer;
    private TextView songNameQueueItem, artistQueueItem;
    private StorageUtil.SettingsStorage settingsStorage;
    private LinearLayoutManager linearLayoutManager;
    private MusicLyricsAdapter lyricsAdapter;
    public ArrayList<String> idList;
    private Dialog timerDialogue;
    private MusicDataCapsule activeMusic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        context = requireContext();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        //StorageUtil initialization
        storageUtil = new StorageUtil(requireContext());

        if (activeMusic == null) {
            activeMusic = getMusic();
        }
        if (activeMusic != null) {
            lrcMap = storageUtil.loadLyrics(activeMusic.getsId());
        }
        settingsStorage = new StorageUtil.SettingsStorage(requireContext());
        mainActivity = (MainActivity) requireContext();

        //Mini player items initializations
        mini_play_view = view.findViewById(R.id.mini_player_layout);//○
        mini_cover = view.findViewById(R.id.song_album_cover_mini);//○
        mini_artist_text = view.findViewById(R.id.song_artist_name_mini);//○
        mini_name_text = view.findViewById(R.id.song_name_mini);//○
        mini_next = view.findViewById(R.id.more_option_i_btn_next);//○
        mini_pause = view.findViewById(R.id.more_option_i_btn_play);//○
        mini_progress = view.findViewById(R.id.mini_player_progress);//○

        //Main player items initializations
        player_layout = view.findViewById(R.id.player_layout);//○
        info_layout = view.findViewById(R.id.linear_info_layout);
        playerCoverImage = view.findViewById(R.id.player_cover_iv);//○
        seekBarMain = view.findViewById(R.id.player_seek_bar);//○
        ImageView queImg = view.findViewById(R.id.player_que_iv);//○
        repeatImg = view.findViewById(R.id.player_repeat_iv);//○
        ImageView previousImg = view.findViewById(R.id.player_previous_iv);//○
        playImg = view.findViewById(R.id.player_play_iv);//○
        ImageView nextImg = view.findViewById(R.id.player_next_iv);//○
        shuffleImg = view.findViewById(R.id.player_shuffle_iv);//○
        favoriteImg = view.findViewById(R.id.player_favorite_iv);//○
        timerImg = view.findViewById(R.id.player_timer_iv);//○
        optionImg = view.findViewById(R.id.player_option_iv);//○
        playerSongNameTv = view.findViewById(R.id.player_song_name_tv);//○
        playerArtistNameTv = view.findViewById(R.id.player_song_artist_name_tv);//○
        bitrateTv = view.findViewById(R.id.player_bitrate_tv);//○
        mimeTv = view.findViewById(R.id.player_mime_tv);//○
        CardView playCv = view.findViewById(R.id.player_play_cv);
        durationTv = view.findViewById(R.id.player_duration_tv);//○
        curPosTv = view.findViewById(R.id.player_current_pos_tv);//○
        lyricsImg = view.findViewById(R.id.player_lyrics_ll);
        timerTv = view.findViewById(R.id.countdown_tv);
        queueCoverImg = view.findViewById(R.id.song_album_cover_queue_item);
        MaterialCardView queueItem = view.findViewById(R.id.queue_music_item);
        songNameQueueItem = view.findViewById(R.id.song_name_queue_item);
        artistQueueItem = view.findViewById(R.id.song_artist_name_queue_item);

        shadowPlayer = view.findViewById(R.id.shadow_player);

        coverCardView = view.findViewById(R.id.card_view_for_cover);
        lyricsRelativeLayout = view.findViewById(R.id.lyrics_relative_layout);

        playerSongNameTv.setSelected(true);
        mini_name_text.setSelected(true);
        songNameQueueItem.setSelected(true);

        setAccorToSettings();

        //click listeners on mini player
        //and sending broadcast on click

        //play pause
        mini_pause.setOnClickListener(v -> mainActivity.pausePlayAudio());
        //next on mini player
        mini_next.setOnClickListener(v -> mainActivity.playNextAudio());
        //main player events
        previousImg.setOnClickListener(v -> mainActivity.playPreviousAudio());
        nextImg.setOnClickListener(v -> mainActivity.playNextAudio());
        playCv.setOnClickListener(v -> mainActivity.pausePlayAudio());
        queImg.setOnClickListener(v -> openQue());
        repeatImg.setOnClickListener(v -> repeatFun());
        shuffleImg.setOnClickListener(v -> shuffleList());
        favoriteImg.setOnClickListener(v -> addFavorite(storageUtil, activeMusic, favoriteImg));
        timerImg.setOnClickListener(v -> setTimer());
        timerTv.setOnClickListener(v -> cancelTimer());
        lyricsImg.setOnClickListener(v -> openLyricsPanel());
        queueItem.setOnClickListener(v -> scrollToCurSong());
        lyricsImg.setOnLongClickListener(view1 -> {
            setLyricsLayout(activeMusic);
            return false;
        });
//        top right option button
        optionImg.setOnClickListener(v -> optionMenu(activeMusic));

        queueRecyclerView = view.findViewById(R.id.queue_music_recycler);
        linearLayoutManager = new LinearLayoutManager(getContext());

        queueBottomSheet = view.findViewById(R.id.queue_bottom_sheet);

        //lyrics layout related initializations
        TextView button = view.findViewById(R.id.btn_add_lyrics);
        lyricsRecyclerView = view.findViewById(R.id.lyrics_recycler_view);
        noLyricsLayout = view.findViewById(R.id.no_lyrics_layout);
        lyricsRecyclerView.addOnScrollListener(onScrollListener);
        button.setOnClickListener(v -> setLyricsLayout(activeMusic));

        seekBarMain.setOnSeekBarChangeListener(this);

        mini_play_view.setOnClickListener(v -> {
            BottomSheetBehavior<View> sheet = mainActivity.mainPlayerSheetBehavior;
            if (sheet.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                sheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                sheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        setupQueueBottomSheet();
        return view;
    }

    /**
     * this is for queue item click
     */
    private void scrollToCurSong() {
        int index = storageUtil.loadMusicIndex();
        linearLayoutManager.scrollToPositionWithOffset(index, 0);
    }

    /**
     * This method sets elements according to settings values
     */
    private void setAccorToSettings() {
        if (settingsStorage.loadShowArtist()) {
            mini_artist_text.setVisibility(View.VISIBLE);
        } else {
            mini_artist_text.setVisibility(View.GONE);
        }
        if (settingsStorage.loadShowInfo()) {
            info_layout.setVisibility(View.VISIBLE);
        } else {
            info_layout.setVisibility(View.GONE);
        }
        if (settingsStorage.loadExtraCon()) {
            mini_next.setVisibility(View.VISIBLE);
        } else {
            mini_next.setVisibility(View.GONE);
        }
        if (settingsStorage.loadOptionMenu()) {
            optionImg.setVisibility(View.VISIBLE);
        } else {
            optionImg.setVisibility(View.GONE);
        }
    }

    private boolean getPlayerState() {
        if (media_player_service.media_player != null)
            return media_player_service.media_player.isPlaying();
        else return false;
    }

    @Subscribe
    public void RemoveLyricsHandler(RemoveLyricsHandlerEvent event) {
        if (lyricsHandler != null) {
            lyricsHandler.removeCallbacks(lyricsRunnable);
        }
    }

    // Don't Remove This Event Bus is using this method (It might look unused still DON'T REMOVE
    @Subscribe
    public void setMainPlayerLayout(SetMainLayoutEvent event) {
        activeMusic = event.activeMusic;

        if (activeMusic != null) {
            songName = activeMusic.getsName();
            bitrate = activeMusic.getsBitrate();
            artistName = activeMusic.getsArtist();
            mimeType = getMime(activeMusic.getsMimeType()).toUpperCase();
            duration = activeMusic.getsLength();
            albumUri = activeMusic.getsAlbumUri();

            String convertedDur = convertDuration(duration);

            //image decoder
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(activeMusic.getsPath());
            byte[] art = mediaMetadataRetriever.getEmbeddedPicture();
            Bitmap image;
            try {
                image = BitmapFactory.decodeByteArray(art, 0, art.length);
            } catch (Exception e) {
                image = null;
            }
            int bitrateInNum = Integer.parseInt(bitrate) / 1000;
            String finalBitrate = bitrateInNum + " KBPS";

            if (storageUtil.checkFavourite(activeMusic.getsId()).equals(no_favorite)) {
                favoriteImg.setImageResource(R.drawable.ic_favorite_border);
            } else if (storageUtil.checkFavourite(activeMusic.getsId()).equals(favorite)) {
                favoriteImg.setImageResource(R.drawable.ic_favorite);
            }

            if (storageUtil.loadShuffle().equals(no_shuffle)) {
                shuffleImg.setImageResource(R.drawable.ic_shuffle_empty);
            } else if (storageUtil.loadShuffle().equals(shuffle)) {
                shuffleImg.setImageResource(R.drawable.ic_shuffle);
            }

            try {
                playerSongNameTv.setText(songName);
                songNameQueueItem.setText(songName);
                playerArtistNameTv.setText(artistName);
                artistQueueItem.setText(artistName);
                mimeTv.setText(mimeType);
                durationTv.setText(convertedDur);
                mini_name_text.setText(songName);
                mini_artist_text.setText(artistName);
                seekBarMain.setMax(Integer.parseInt(duration));
                mini_progress.setMax(Integer.parseInt(duration));
                bitrateTv.setText(finalBitrate);

                GlideBuilt.glideBitmap(requireContext(), image, R.drawable.ic_music, playerCoverImage, 512);
                GlideBuilt.glide(requireContext(), albumUri, R.drawable.ic_music, mini_cover, 128);
                GlideBuilt.glide(requireContext(), albumUri, R.drawable.ic_music, queueCoverImg, 128);

                ((MainActivity) context).setDataInNavigation(songName, artistName, image);

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        EventBus.getDefault().post(new RunnableSyncLyricsEvent());
    }

    @SuppressLint("SetTextI18n")
    public void resetMainPlayerLayout() {
        playerSongNameTv.setText("Song");
        playerArtistNameTv.setText("Artist");
        mimeTv.setText("MP3");
        bitrateTv.setText("0 KBPS");
        curPosTv.setText("00:00");
        durationTv.setText("-00:00");
        mini_name_text.setText("Song Name");
        mini_artist_text.setText("Artist Name");
        seekBarMain.setMax(0);
        mini_progress.setMax(0);
        seekBarMain.setProgress(0);
        mini_progress.setProgress(0);
        GlideBuilt.glide(requireContext(), null, R.drawable.ic_music, playerCoverImage, 512);
        GlideBuilt.glide(requireContext(), null, R.drawable.ic_music, mini_cover, 128);

    }

    @Subscribe
    public void runnableSyncLyrics(RunnableSyncLyricsEvent event) {
        if (activeMusic != null) {
            lrcMap = storageUtil.loadLyrics(activeMusic.getsId());
            if (lrcMap != null) {
                noLyricsLayout.setVisibility(View.GONE);
                lyricsRecyclerView.setVisibility(View.VISIBLE);
                lyricsArrayList.clear();
                lyricsArrayList.addAll(lrcMap.getLyrics());
                setLyricsAdapter();
                lyricsHandler = new Handler(Looper.getMainLooper());
                EventBus.getDefault().post(new PrepareRunnableEvent());
            } else {
                lyricsRecyclerView.setVisibility(View.GONE);
                noLyricsLayout.setVisibility(View.VISIBLE);
            }
        }

    }

    // Don't Remove This Event Bus is using this method (It might look unused still DON'T REMOVE
    @Subscribe
    public void prepareRunnable(PrepareRunnableEvent event) {
        if (lyricsHandler != null) {
            lyricsRunnable = () -> {
                int nextStampInMillis = 0;
                int currPosInMillis = 0;
                if (media_player_service != null) {
                    if (!getPlayerState()) return;

                    String nextStamp = getNextStamp(lrcMap);
                    if (!nextStamp.equals("")) {
                        nextStampInMillis = MusicHelper.convertToMillis(nextStamp);
                        currPosInMillis = getCurrentPos();
                    }
                }
                if (lrcMap != null) {
                    if (lyricsRecyclerView.getVisibility() == View.VISIBLE) {
                        if (!userScrolling)
                            if (lrcMap.containsStamp(getCurrentStamp())) {
                                scrollToPosition(lrcMap.getIndexAtStamp(getCurrentStamp()));
                            }
                    }
                }
                lyricsHandler.postDelayed(lyricsRunnable, nextStampInMillis - currPosInMillis);

            };
            lyricsHandler.postDelayed(lyricsRunnable, 0);
        }
    }

    @Subscribe
    public void handleMusicProgressUpdate(UpdateMusicProgressEvent event) {
        mini_progress.setProgress(event.position);
        seekBarMain.setProgress(event.position);
        String cur = convertDuration(String.valueOf(event.position));
        curPosTv.setText(cur);
    }

    @Subscribe
    public void handleMusicImageUpdate(UpdateMusicImageEvent event) {
        if (event.shouldDisplayPlayImage) {
            mini_pause.setImageResource(R.drawable.ic_play_mini);
            playImg.setImageResource(R.drawable.ic_play_main);
        } else {
            mini_pause.setImageResource(R.drawable.ic_pause_mini);
            playImg.setImageResource(R.drawable.ic_pause_main);
        }
    }

    public String getCurrentStamp() {
        int currPosInMillis = 0;
        if (media_player_service != null)
            currPosInMillis = media_player_service.media_player.getCurrentPosition();

        return "[" + convertDuration(String.valueOf(currPosInMillis)) + "]";
    }

    public String getNextStamp(LRCMap _lrcMap) {
        String curStamp = getCurrentStamp();
        int currIndex = -1;
        if (_lrcMap != null) {
            currIndex = _lrcMap.getIndexAtStamp(curStamp);
        }
        if (currIndex == -1) return "";
        return currIndex == _lrcMap.size() - 1 ? _lrcMap.getStampAt(currIndex) : _lrcMap.getStampAt(currIndex + 1);

    }

    private void scrollToPosition(int position) {
        RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScrollScript.CenterSmoothScroller(context);
        smoothScroller.setTargetPosition(position);
        lm.startSmoothScroll(smoothScroller);
    }

    //give the mime type value and it will return extension
    public String getMime(String filePath) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(filePath);
    }

    public void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public int getCurrentPos() {
        return media_player_service.media_player.getCurrentPosition();
    }

    private void setLyricsAdapter() {
        lm = new LinearLayoutManager(context);// or whatever layout manager you need

        lyricsRecyclerView.setLayoutManager(lm);

        lyricsAdapter = new MusicLyricsAdapter(context, lyricsArrayList);
        lyricsRecyclerView.setAdapter(lyricsAdapter);

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void setLyricsLayout(MusicDataCapsule selectedMusic) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("AddLyricsFragment");
        if (fragment != null) {
            fragmentManager.popBackStackImmediate();
        }
        Bundle music = new Bundle();
        music.putSerializable("selectedMusic", selectedMusic);
        AddLyricsFragment addLyricsFragment = new AddLyricsFragment();
        addLyricsFragment.setArguments(music);
        addLyricsFragment.setEnterTransition(TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.slide_top));

        if (mainActivity.mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mainActivity.mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.sec_container, addLyricsFragment, "AddLyricsFragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupQueueBottomSheet() {
        queueRecyclerView.setLayoutManager(linearLayoutManager);
        idList = storageUtil.loadQueueList();
        setQueueAdapter();

        queueSheetBehaviour = (CustomBottomSheet<View>) BottomSheetBehavior.from(queueBottomSheet);
        queueSheetBehaviour.setHideable(true);
        queueSheetBehaviour.setSkipCollapsed(true);
        queueSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        queueSheetBehaviour.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                ((MainActivity) context).mainPlayerSheetBehavior.isEnableCollapse(newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_DRAGGING);
                if (queueSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    shadowPlayer.setAlpha(1);
                } else if (queueSheetBehaviour.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    shadowPlayer.setAlpha(0);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                bottomSheet.setAlpha(0 + (slideOffset + 1f));
                shadowPlayer.setAlpha(0 + (slideOffset + 1f));
            }
        });

    }

    /**
     * Setting adapter in queue list
     */
    private void setQueueAdapter() {
        if (idList != null) {
            ArrayList<MusicDataCapsule> list = storageUtil.getItemListFromInitialList(idList);
            queueAdapter = new MusicQueueAdapter(getActivity(), list, this);
            queueRecyclerView.setAdapter(queueAdapter);
            ItemTouchHelper.Callback callback = new SimpleTouchCallback(queueAdapter);
            itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(queueRecyclerView);
        }
    }

    /**
     * Updating adapter in queue list
     */
    public void updateQueueAdapter(ArrayList<String> _idsList) {
        if (idList != null) {
            idList.clear();
            idList.addAll(_idsList);
            queueAdapter.updateView();
        } else {
            idList = new ArrayList<>();
            idList.addAll(_idsList);

            ArrayList<MusicDataCapsule> list = storageUtil.getItemListFromInitialList(idList);
            queueAdapter = new MusicQueueAdapter(getActivity(), list, this);
            queueRecyclerView.setAdapter(queueAdapter);
            ItemTouchHelper.Callback callback = new SimpleTouchCallback(queueAdapter);
            itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(queueRecyclerView);
        }
    }

    /**
     * it sets visibility of cover image and lyrics recyclerView
     */
    private void openLyricsPanel() {
        if (coverCardView.getVisibility() == View.VISIBLE) {
            lyricsImg.setImageResource(R.drawable.ic_lyrics_off);
            coverCardView.setVisibility(View.GONE);
            lyricsRelativeLayout.setVisibility(View.VISIBLE);
            lyricsRelativeLayout.setKeepScreenOn(settingsStorage.loadKeepScreenOn());
        } else if (coverCardView.getVisibility() == View.GONE) {
            lyricsImg.setImageResource(R.drawable.ic_lyrics);
            coverCardView.setVisibility(View.VISIBLE);
            lyricsRelativeLayout.setVisibility(View.GONE);
            lyricsRelativeLayout.setKeepScreenOn(false);
        }
    }

    private void optionMenu(MusicDataCapsule activeMusic) {
        //add a bottom sheet to show music options like set to ringtone ,audio details ,add to playlist etc.
        if (activeMusic != null)
            mainActivity.openOptionMenu(activeMusic, "mainList");
    }

    //region Timer setup
    private void setTimer() {
        //Create a dialogue Box
        timerDialogue = new Dialog(context);
        timerDialogue.requestWindowFeature(Window.FEATURE_NO_TITLE);
        timerDialogue.setCancelable(true);
        timerDialogue.setContentView(R.layout.timer_dialogue);

        //Initialize Dialogue Box UI Items
        TextView showTimeText = timerDialogue.findViewById(R.id.timer_time_textview);
        SeekBar timerSeekBar = timerDialogue.findViewById(R.id.timer_time_seekbar);
        Button timerConfirmButton = timerDialogue.findViewById(R.id.timer_confirm_button);

        //Set TextView Initially based on seekbar progress
        String finalTextTime = String.valueOf(timerSeekBar.getProgress() + 5);
        showTimeText.setText(finalTextTime);

        //Update Text based on Seekbar Progress
        timerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                progress = progress * 5;
                String finalProgress = String.valueOf(progress + 5);
                showTimeText.setText(finalProgress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //Dialogue Box Confirm Button Listener
        timerConfirmButton.setOnClickListener(view -> {
            timerDialogue.dismiss();
            timerTv.setVisibility(View.VISIBLE);
            timerImg.setVisibility(View.GONE);

            //Sets The already Initialized countDownTimer to a new countDownTimer with given parameters
            countDownTimer[0] = new CountDownTimer((timerSeekBar.getProgress() * 5L + 5) * 1000L * 60, 1000) {

                //Variables For storing seconds and minutes
                int seconds;
                int minutes;

                //Every Second Do Something
                //Update TextView Code Goes Here
                @Override
                public void onTick(long l) {

                    //Storing Seconds and Minutes on Every Tick
                    seconds = (int) (l / 1000) % 60;
                    minutes = (int) ((l / (1000 * 60)) % 60);

                    // Replace This with TextView.setText(View);
                    String finalCDTimer = minutes + ":" + seconds;
                    timerTv.setText(finalCDTimer);
                }


                //Code After timer is Finished Goes Here
                @Override
                public void onFinish() {

                    //Replace This pausePlayAudio() with just a pause Method.
                    //Replaced it
                    media_player_service.pauseMedia();
                    timerTv.setVisibility(View.GONE);
                    timerImg.setVisibility(View.VISIBLE);
                }
            };
            // Start timer
            countDownTimer[0].start();
        });
        //Show Timer Dialogue Box
        timerDialogue.show();


    }

    private void cancelTimer() {
        // Else if timer Icon is set to ic_timer already then execute this
        // Cancel any previous set timer
        countDownTimer[0].cancel();
        timerTv.setVisibility(View.GONE);
        timerImg.setVisibility(View.VISIBLE);
    }

    public void addFavorite(StorageUtil storageUtil, MusicDataCapsule activeMusic,@Nullable ImageView imageView) {
        if (activeMusic != null) {
            if (storageUtil.checkFavourite(activeMusic.getsId()).equals(no_favorite)) {
                storageUtil.saveFavorite(activeMusic.getsId());
                if (imageView != null) {
                    imageView.setImageResource(R.drawable.ic_favorite);
                }
            } else if (storageUtil.checkFavourite(activeMusic.getsId()).equals(favorite)) {
                storageUtil.removeFavorite(activeMusic.getsId());
                if (imageView != null) {
                    imageView.setImageResource(R.drawable.ic_favorite_border);
                }
            }
        }
    }

    private MusicDataCapsule getMusic() {
        ArrayList<String> musicList = storageUtil.loadQueueList();
        MusicDataCapsule activeMusic = null;
        int musicIndex;
        musicIndex = storageUtil.loadMusicIndex();

        if (musicList != null && musicList.size() != 0)
            if (musicIndex != -1 && musicIndex < musicList.size()) {
                String id = musicList.get(musicIndex);
                activeMusic = storageUtil.getItemFromInitialList(id);
            } else {
                activeMusic = storageUtil.getItemFromInitialList(musicList.get(0));
            }
        return activeMusic;
    }

    private void shuffleList() {
        //shuffle list program
        if (storageUtil.loadShuffle().equals(no_shuffle)) {
            shuffleImg.setImageResource(R.drawable.ic_shuffle);
            storageUtil.saveShuffle(shuffle);
            shuffleListAndSave(activeMusic);
        } else if (storageUtil.loadShuffle().equals(shuffle)) {
            shuffleImg.setImageResource(R.drawable.ic_shuffle_empty);
            storageUtil.saveShuffle(no_shuffle);
            restoreLastListAndPos(activeMusic);
        }
    }

    private void shuffleListAndSave(MusicDataCapsule activeMusic) {
        ArrayList<String> musicIDList = storageUtil.loadQueueList();
        storageUtil.saveTempMusicList(musicIDList);
        int musicIndex;
        musicIndex = storageUtil.loadMusicIndex();

        // do in background code here
        if (musicIDList != null) {
            shuffleImg.setClickable(false);

            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            service.execute(() -> {
                //removing current item from list
                musicIDList.remove(musicIndex);
                //shuffling list
                Collections.shuffle(musicIDList);
                //adding the removed item in shuffled list on 0th index
                musicIDList.add(0, activeMusic.getsId());
                //saving list
                storageUtil.saveQueueList(musicIDList);
                //saving index
                storageUtil.saveMusicIndex(0);
                // post-execute code here
                handler.post(() -> {
                    updateQueueAdapter(musicIDList);
                    shuffleImg.setClickable(true);
                    // stopping the background thread (crucial)
                });
            });
            service.shutdown();
        }


    }

    private void restoreLastListAndPos(MusicDataCapsule activeMusic) {
        ArrayList<String> tempIdList = storageUtil.loadTempMusicList();

        final int[] curIndex = new int[1];

        if (tempIdList != null) {
            shuffleImg.setClickable(false);

            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            // do in background code here
            service.execute(() -> {
                curIndex[0] = activeMusicIndexFinder(activeMusic, tempIdList);
                if (curIndex[0] != -1) {
                    storageUtil.saveMusicIndex(curIndex[0]);
                }
                storageUtil.saveQueueList(tempIdList);
                // post-execute code here
                handler.post(() -> {
                    updateQueueAdapter(tempIdList);
                    shuffleImg.setClickable(true);
                });
            });
            service.shutdown();
        }
    }

    private int activeMusicIndexFinder(MusicDataCapsule activeMusic, @NonNull ArrayList<String> idList) {
        int index;

        ArrayList<MusicDataCapsule> list = storageUtil.getItemListFromInitialList(idList);
        for (index = 0; index < list.size(); ++index) {
            if (list.get(index).getsName().equals(activeMusic.getsName()) && list.get(index).getsLength().equals(activeMusic.getsLength())) {
                return index;
            }
        }
        return -1;
    }

    private void repeatFun() {
        //function for music list and only one music repeat and save that state in sharedPreference
        if (storageUtil.loadRepeatStatus().equals(no_repeat)) {
            repeatImg.setImageResource(R.drawable.ic_repeat);
            storageUtil.saveRepeatStatus(repeat);
        } else if (storageUtil.loadRepeatStatus().equals(repeat)) {
            repeatImg.setImageResource(R.drawable.ic_repeat_one);
            storageUtil.saveRepeatStatus(repeat_one);
        } else if (storageUtil.loadRepeatStatus().equals(repeat_one)) {
            repeatImg.setImageResource(R.drawable.ic_repeat_empty);
            storageUtil.saveRepeatStatus(no_repeat);
        }
    }

    private void openQue() {
        queueSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
        queueBottomSheet.setAlpha(1);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            setPreviousData(activeMusic);
        } catch (Exception ignored) {
        }

        //setting all buttons state from storage on startup
        //for repeat button
        if (storageUtil.loadRepeatStatus().equals(no_repeat)) {
            repeatImg.setImageResource(R.drawable.ic_repeat_empty);
        } else if (storageUtil.loadRepeatStatus().equals(repeat)) {
            repeatImg.setImageResource(R.drawable.ic_repeat);
        } else if (storageUtil.loadRepeatStatus().equals(repeat_one)) {
            repeatImg.setImageResource(R.drawable.ic_repeat_one);
        }

        //for shuffle button
        if (storageUtil.loadShuffle().equals(no_shuffle)) {
            shuffleImg.setImageResource(R.drawable.ic_shuffle_empty);
        } else if (storageUtil.loadShuffle().equals(shuffle)) {
            shuffleImg.setImageResource(R.drawable.ic_shuffle);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        lyricsRecyclerView.clearOnScrollListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onResume() {
        super.onResume();
        //On Start Favourite Code Moved Here
        //for favorite

        //layout setup ☺
        if (service_bound) {
            if (media_player_service != null)
                if (media_player_service.media_player != null) {
                    if (media_player_service.media_player.isPlaying()) {
                        media_player_service.setIcon(PlaybackStatus.PLAYING);
                    } else {
                        media_player_service.setIcon(PlaybackStatus.PAUSED);
                    }
                }
        }
        setButton(activeMusic);

        if (mainActivity.mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mini_play_view.setAlpha(0);
            mini_play_view.setVisibility(View.INVISIBLE);
        } else if (mainActivity.mainPlayerSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            player_layout.setAlpha(0);
            player_layout.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * just for startup
     *
     * @param activeMusic
     */
    private void setButton(MusicDataCapsule activeMusic) {
        if (activeMusic != null) {
            if (storageUtil.checkFavourite(activeMusic.getsId()).equals(no_favorite)) {
                favoriteImg.setImageResource(R.drawable.ic_favorite_border);
            } else if (storageUtil.checkFavourite(activeMusic.getsId()).equals(favorite)) {
                favoriteImg.setImageResource(R.drawable.ic_favorite);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            curPosTv.setText(convertDuration(String.valueOf(progress)));
            mini_progress.setProgress(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //removing handler so we can change position of seekbar
        if (service_bound) {
            if (media_player_service.seekBarHandler != null)
                media_player_service.seekBarHandler.removeCallbacks(media_player_service.seekBarRunnable);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //clearing the storage before putting new value
        storageUtil.clearMusicLastPos();

        //storing the current position of seekbar in storage so we can access it from services
        storageUtil.saveMusicLastPos(seekBar.getProgress());

        //first checking setting the media seek to current position of seek bar and then setting all data in UI
        if (service_bound) {
            if (media_player_service.seekBarHandler != null)
                media_player_service.seekBarHandler.removeCallbacks(media_player_service.seekBarRunnable);
            if (media_player_service.media_player != null) {
                media_player_service.media_player.seekTo(seekBar.getProgress());
                if (media_player_service.media_player.isPlaying()) {
                    media_player_service.buildNotification(PlaybackStatus.PLAYING, 1f);
                } else {
                    media_player_service.buildNotification(PlaybackStatus.PAUSED, 0f);
                }
            }
            media_player_service.setSeekBar();
        } else {
            mini_progress.setProgress(seekBar.getProgress());
        }
    }

    /**
     * seekTo the position of line you clicked in lyrics
     *
     * @param pos clicked lyric time stamp position
     */
    public void skipToPosition(int pos) {
        scrollToPosition(pos);
        int position = convertToMillis(lrcMap.getStampAt(pos));
        if (lrcMap != null) {
            seekBarMain.setProgress(position);
            curPosTv.setText(convertDuration(String.valueOf(position)));
            //clearing the storage before putting new value
            storageUtil.clearMusicLastPos();

            //storing the current position of seekbar in storage so we can access it from services
            storageUtil.saveMusicLastPos(position);

            //first checking setting the media seek to current position of seek bar and then setting all data in UI
            if (service_bound) {
                if (media_player_service.seekBarHandler != null)
                    media_player_service.seekBarHandler.removeCallbacks(media_player_service.seekBarRunnable);
                if (media_player_service.media_player != null) {
                    media_player_service.media_player.seekTo(position);
                    if (media_player_service.media_player.isPlaying()) {
                        media_player_service.buildNotification(PlaybackStatus.PLAYING, 1f);
                    } else {
                        media_player_service.buildNotification(PlaybackStatus.PAUSED, 0f);
                    }
                }
                media_player_service.setSeekBar();
            } else {
                mini_progress.setProgress(position);
            }
        }
    }

    public void setPreviousData(MusicDataCapsule activeMusic) {

        EventBus.getDefault().post(new SetMainLayoutEvent(activeMusic));

        if (activeMusic != null) {
            seekBarMain.setMax(Integer.parseInt(activeMusic.getsLength()));
            mini_progress.setMax(Integer.parseInt(activeMusic.getsLength()));
            durationTv.setText(convertDuration(activeMusic.getsLength()));

            int resumePosition = storageUtil.loadMusicLastPos();
            if (resumePosition != -1) {
                seekBarMain.setProgress(resumePosition);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mini_progress.setProgress(resumePosition, true);
                } else {
                    mini_progress.setProgress(resumePosition);
                }
                String cur = convertDuration(String.valueOf(resumePosition));
                curPosTv.setText(cur);
            }
        }
    }

    @Override
    public void onDragStart(RecyclerView.ViewHolder viewHolder) {
        if (idList != null) {
            itemTouchHelper.startDrag(viewHolder);
        }
    }

}