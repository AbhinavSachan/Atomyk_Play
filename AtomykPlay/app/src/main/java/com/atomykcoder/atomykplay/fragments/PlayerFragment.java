package com.atomykcoder.atomykplay.fragments;

import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PAUSE_PLAY_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PLAY_NEXT_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PLAY_PREVIOUS_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.media_player_service;
import static com.atomykcoder.atomykplay.MainActivity.service_bound;
import static com.atomykcoder.atomykplay.MainActivity.service_connection;
import static com.atomykcoder.atomykplay.function.FetchMusic.convertDuration;
import static com.atomykcoder.atomykplay.function.StorageUtil.favorite;
import static com.atomykcoder.atomykplay.function.StorageUtil.no_favorite;
import static com.atomykcoder.atomykplay.function.StorageUtil.no_repeat;
import static com.atomykcoder.atomykplay.function.StorageUtil.no_shuffle;
import static com.atomykcoder.atomykplay.function.StorageUtil.repeat;
import static com.atomykcoder.atomykplay.function.StorageUtil.repeat_one;
import static com.atomykcoder.atomykplay.function.StorageUtil.shuffle;
import static com.atomykcoder.atomykplay.services.MediaPlayerService.phone_ringing;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.MainActivity;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.function.FetchLyrics;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.MusicQueueAdapter;
import com.atomykcoder.atomykplay.function.PlaybackStatus;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.interfaces.SimpleTouchCallback;
import com.atomykcoder.atomykplay.services.MediaPlayerService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
<<<<<<< Updated upstream
import java.util.Locale;
=======
>>>>>>> Stashed changes
import java.util.concurrent.ExecutionException;

@SuppressLint("StaticFieldLeak")
public class PlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, OnDragStartListener {

    public static RelativeLayout mini_play_view;
    public static View player_layout;
    public static ImageView mini_cover, mini_pause, mini_next;
    public static LinearProgressIndicator mini_progress;
    public static TextView mini_name_text, mini_artist_text;
    //main player seekbar
    public static SeekBar seekBarMain;
    public static ImageView playImg;
    public static TextView curPosTv, durationTv;
    public static CustomBottomSheet<View> queueSheetBehaviour;
    public static View queueBottomSheet;
    private static Context context;
    //cover image view
    private static ImageView playerCoverImage;
    private static ImageView repeatImg;
    private static ImageView shuffleImg;
    private static ImageView favoriteImg;
    private static ImageView timerImg;
    private static TextView playerSongNameTv, playerArtistNameTv, mimeTv, bitrateTv, timerTv;
    final private CountDownTimer[] countDownTimer = new CountDownTimer[1];
    //setting up mini player layout
    //calling it from service when player is prepared and also calling it in this fragment class
    //to set it on app start ☺
    private StorageUtil storageUtil;
    private ItemTouchHelper itemTouchHelper;
    private RecyclerView recyclerView;

    public static void setMiniLayout() {

        StorageUtil storageUtil = new StorageUtil(context);
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

        try {
            if (activeMusic != null) {
                Glide.with(context).load(getEmbeddedImage(activeMusic.getsPath())).apply(new RequestOptions().placeholder(R.drawable.ic_music))
                        .override(75, 75)
                        .into(mini_cover);
                try {
                    mini_name_text.setText(activeMusic.getsName());
                    mini_artist_text.setText(activeMusic.getsArtist());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setMainPlayerLayout() {

        StorageUtil storageUtil = new StorageUtil(context);
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


        if (activeMusic != null) {
            if (storageUtil.loadFavorite(activeMusic.getsName()).equals("no_favorite")) {
                favoriteImg.setImageResource(R.drawable.ic_favorite_border);
            } else if (storageUtil.loadFavorite(activeMusic.getsName()).equals("favorite")) {
                favoriteImg.setImageResource(R.drawable.ic_favorite);
            }
        }

        try {
            if (activeMusic != null) {
                Glide.with(context).load(getEmbeddedImage(activeMusic.getsPath())).apply(new RequestOptions().placeholder(R.drawable.ic_music_thumbnail))
                        .override(500, 500)
                        .into(playerCoverImage);

                try {
                    playerSongNameTv.setText(activeMusic.getsName());
                    playerArtistNameTv.setText(activeMusic.getsArtist());
                    mimeTv.setText(getMime(activeMusic.getsMimeType()).toUpperCase());
                    durationTv.setText(convertDuration(activeMusic.getsLength()));

                    int bitrateInNum = Integer.parseInt(activeMusic.getsBitrate()) / 1000;
                    String finalBitrate = bitrateInNum + " KBPS";
                    bitrateTv.setText(finalBitrate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //give the mime type value and it will return extension
    public static String getMime(String filePath) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(filePath);
    }

    public static Bitmap getEmbeddedImage(String songPath) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(songPath);
        byte[] data = metadataRetriever.getEmbeddedPicture();
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } else {
            return null;
        }
    }

    public static void pausePlayAudio() {
        if (!phone_ringing) {
            if (!service_bound) {
                Intent playerIntent = new Intent(context, MediaPlayerService.class);
                context.startService(playerIntent);
                context.bindService(playerIntent, service_connection, Context.BIND_AUTO_CREATE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //service is active send media with broadcast receiver
                        Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
                        context.sendBroadcast(broadcastIntent);
                    }
                }, 0);
            } else {
                //service is active send media with broadcast receiver
                Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
                context.sendBroadcast(broadcastIntent);
            }
        } else {
            Toast.makeText(context, "Can't play while on call", Toast.LENGTH_SHORT).show();
        }

    }

    public static void playNextAudio() {
        if (!phone_ringing) {
            if (!service_bound) {
                Intent playerIntent = new Intent(context, MediaPlayerService.class);
                context.startService(playerIntent);
                context.bindService(playerIntent, service_connection, Context.BIND_AUTO_CREATE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //service is active send media with broadcast receiver
                        Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEXT_MUSIC);
                        context.sendBroadcast(broadcastIntent);
                    }
                }, 0);
            } else {
                //service is active send media with broadcast receiver
                Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEXT_MUSIC);
                context.sendBroadcast(broadcastIntent);
            }
        } else {
            Toast.makeText(context, "Can't play while on call", Toast.LENGTH_SHORT).show();
        }
    }

    public static void playPreviousAudio() {
        if (!phone_ringing) {
            if (!service_bound) {
                Intent playerIntent = new Intent(context, MediaPlayerService.class);
                context.startService(playerIntent);
                context.bindService(playerIntent, service_connection, Context.BIND_AUTO_CREATE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //service is active send media with broadcast receiver
                        Intent broadcastIntent = new Intent(BROADCAST_PLAY_PREVIOUS_MUSIC);
                        context.sendBroadcast(broadcastIntent);
                    }
                }, 0);

            } else {
                //service is active send media with broadcast receiver
                Intent broadcastIntent = new Intent(BROADCAST_PLAY_PREVIOUS_MUSIC);
                context.sendBroadcast(broadcastIntent);
            }
        } else {
            Toast.makeText(context, "Can't play while on call", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        try {
            context = getContext();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        //StorageUtil initialization
        storageUtil = new StorageUtil(getContext());

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
        ImageView optionImg = view.findViewById(R.id.player_option_iv);//○
        playerSongNameTv = view.findViewById(R.id.player_song_name_tv);//○
        playerArtistNameTv = view.findViewById(R.id.player_song_artist_name_tv);//○
        bitrateTv = view.findViewById(R.id.player_bitrate_tv);//○
        mimeTv = view.findViewById(R.id.player_mime_tv);//○
        durationTv = view.findViewById(R.id.player_duration_tv);//○
        curPosTv = view.findViewById(R.id.player_current_pos_tv);//○
        ImageView lyricsOpenLayout = view.findViewById(R.id.player_lyrics_ll);
        timerTv = view.findViewById(R.id.countdown_tv);

        playerSongNameTv.setSelected(true);
        mini_name_text.setSelected(true);

        //click listeners on mini player
        //and sending broadcast on click

        //play pause
        mini_pause.setOnClickListener(v -> pausePlayAudio());
        //next on mini player
        mini_next.setOnClickListener(v -> playNextAudio());
        //main player events
        previousImg.setOnClickListener(v -> playPreviousAudio());
        nextImg.setOnClickListener(v -> playNextAudio());
        playImg.setOnClickListener(v -> pausePlayAudio());
        queImg.setOnClickListener(v -> openQue());
        repeatImg.setOnClickListener(v -> repeatFun());
        shuffleImg.setOnClickListener(v -> shuffleList());
        favoriteImg.setOnClickListener(v -> addFavorite());
        timerImg.setOnClickListener(v -> setTimer());
        timerTv.setOnClickListener(v -> cancelTimer());
        lyricsOpenLayout.setOnClickListener(v -> openLyricsPanel());
        //top right option button
        optionImg.setOnClickListener(v -> optionMenu());

        recyclerView = view.findViewById(R.id.queue_music_recycler);
        queueBottomSheet = view.findViewById(R.id.bottom_sheet);


        queueBottomSheet.setClickable(true);
        seekBarMain.setClickable(true);

        seekBarMain.setOnSeekBarChangeListener(this);

        mini_play_view.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) context;
            BottomSheetBehavior<View> sheet = mainActivity.bottomSheetBehavior;
            if (sheet.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                sheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                sheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        setupQueueBottomSheet();

        return view;
    }

    private void setupQueueBottomSheet() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter();

        queueSheetBehaviour = (CustomBottomSheet<View>) BottomSheetBehavior.from(queueBottomSheet);
        queueSheetBehaviour.setHideable(true);
        queueSheetBehaviour.setSkipCollapsed(true);
        queueSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        queueSheetBehaviour.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                ((MainActivity)context).bottomSheetBehavior.isEnableCollapse(newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_DRAGGING);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                bottomSheet.setAlpha(0 + slideOffset * 4);
            }
        });

    }

    private void setAdapter() {
        ArrayList<MusicDataCapsule> dataList;
        dataList = new StorageUtil(getContext()).loadMusicList();
        MusicQueueAdapter adapter = new MusicQueueAdapter(getActivity(), dataList, this);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleTouchCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    private void openLyricsPanel() {
        MainActivity mainActivity = (MainActivity)context;
        mainActivity.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        AddLyricsFragment addLyricsFragment = new AddLyricsFragment();
        FragmentManager manager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.sec_container, addLyricsFragment);
        transaction.addToBackStack(addLyricsFragment.toString());
        transaction.commit();
    }


    private void fetchLyrics() {
        //show lyrics in bottom sheet
<<<<<<< Updated upstream
        showToast("lyrics");
        FetchLyrics fetchLyrics = new FetchLyrics();

        String trackName = playerSongNameTv.getText().toString();
        String artistName = playerArtistNameTv.getText().toString();
        trackName = evalSongName(trackName, artistName);
        Log.i("SONG", "Track name: " + trackName);
        try {
            fetchLyrics.execute(trackName, artistName);
            String subtitles = fetchLyrics.get();
            Log.i("SONG", subtitles);
        } catch(Exception e) {
=======
        showToast("fetching");

        try {
            FetchLyrics fetchLyrics = new FetchLyrics();
            //songLength format 02:39.36
            fetchLyrics.execute("SongName + Artist + SongLength");

        } catch (Exception e) {
>>>>>>> Stashed changes
            e.printStackTrace();
        }
    }
    public static void setLyrics(String lyrics) {

    }

    private String evalSongName(String song, String artist) {
        song = song.toLowerCase();
        artist = artist.toLowerCase();
        if(song.contains("[")) {
            song = song.substring(0, song.indexOf("["));
        }
        if(song.contains("(")) {
            song = song.substring(0, song.indexOf("("));
        }
        if(song.contains("{")) {
            song = song.substring(0, song.indexOf("{"));
        }
        if(song.contains(artist)){
            song = song.replace(artist, "");
        }
        if(song.contains("-")){
            song = song.replace("-", "");
        }
        return song;
    }

    private void optionMenu() {
        //add a bottom sheet to show music options like set to ringtone ,audio details ,add to playlist etc.
        showToast("option");

    }

    //region Timer setup
    private void setTimer() {
        //Create a dialogue Box
        final Dialog timerDialogue = new Dialog(PlayerFragment.context);
        timerDialogue.requestWindowFeature(Window.FEATURE_NO_TITLE);
        timerDialogue.setCancelable(true);
        timerDialogue.setContentView(R.layout.timer_dialogue);

        //Initialize Dialogue Box UI Items
        TextView showTimeText = timerDialogue.findViewById(R.id.timer_time_textview);
        SeekBar timerSeekBar = timerDialogue.findViewById(R.id.timer_time_seekbar);
        Button timerConfirmButton = timerDialogue.findViewById(R.id.timer_confirm_button);

        //Set TextView Initially based on seekbar progress
        String finalTextTime = timerSeekBar.getProgress() + 5 + " Minutes";
        showTimeText.setText(finalTextTime);

        //Update Text based on Seekbar Progress
        timerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                progress = progress * 5;
                String finalProgress = progress + 5 + " Minutes";
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
        timerConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
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

    private void addFavorite() {
        MusicDataCapsule activeMusic = getMusic();
        if (activeMusic != null) {
            if (storageUtil.loadFavorite(activeMusic.getsName()).equals(no_favorite)) {
                favoriteImg.setImageResource(R.drawable.ic_favorite);
                storageUtil.saveFavorite(activeMusic.getsName());
            } else if (storageUtil.loadFavorite(activeMusic.getsName()).equals(favorite)) {
                favoriteImg.setImageResource(R.drawable.ic_favorite_border);
                storageUtil.removeFavorite(activeMusic.getsName());
            }
        }
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

    private void shuffleList() {
        //shuffle list program
        if (storageUtil.loadShuffle().equals(no_shuffle)) {
            shuffleImg.setImageResource(R.drawable.ic_shuffle);
            storageUtil.saveShuffle(shuffle);
            shuffleListAndSave();
        } else if (storageUtil.loadShuffle().equals(shuffle)) {
            shuffleImg.setImageResource(R.drawable.ic_shuffle_empty);
            storageUtil.saveShuffle(no_shuffle);
            restoreLastListAndPos();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void shuffleListAndSave() {
        ArrayList<MusicDataCapsule> musicList = storageUtil.loadMusicList();
        MusicDataCapsule activeMusic = getMusic();
        storageUtil.saveTempMusicList(musicList);
        int musicIndex;
        musicIndex = storageUtil.loadMusicIndex();

        //removing current item from list
        musicList.remove(musicIndex);
        //shuffling list
        Collections.shuffle(musicList);
        //adding the removed item in shuffled list on 0th index
        musicList.add(0, activeMusic);
        //saving list
        storageUtil.saveMusicList(musicList);
        //saving index
        storageUtil.saveMusicIndex(0);
        setAdapter();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void restoreLastListAndPos() {
        ArrayList<MusicDataCapsule> tempList = storageUtil.loadTempMusicList();
        MusicDataCapsule activeMusic = getMusic();
        int curIndex;

        curIndex = activeMusicIndexFinder(activeMusic, tempList);
        if (curIndex != -1) {
            storageUtil.saveMusicIndex(curIndex);
        }
        storageUtil.saveMusicList(tempList);
        setAdapter();
    }

    private int activeMusicIndexFinder(MusicDataCapsule activeMusic, ArrayList<MusicDataCapsule> list) {
        int index;

        for (index = 0; index < list.size(); ++index) {
            if (list.get(index).getsName().equals(activeMusic.getsName()) && list.get(index).getsLength().equals(activeMusic.getsLength())) {
                Log.d("Position", String.valueOf(index));
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
        setPreviousData();
        setMiniLayout();
        setMainPlayerLayout();

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
    }

    @Override
    public void onResume() {
        super.onResume();
        //On Start Favourite Code Moved Here
        //for favorite

        //layout setup ☺
        if (service_bound) {

            if (media_player_service.media_player != null) {
                if (media_player_service.media_player.isPlaying()) {
                    media_player_service.setIcon(PlaybackStatus.PLAYING);
                } else {
                    media_player_service.setIcon(PlaybackStatus.PAUSED);
                }
            }
        }
        MusicDataCapsule activeMusic = getMusic();
        if (activeMusic != null) {
            if (storageUtil.loadFavorite(activeMusic.getsName()).equals(no_favorite)) {
                favoriteImg.setImageResource(R.drawable.ic_favorite_border);
            } else if (storageUtil.loadFavorite(activeMusic.getsName()).equals(favorite)) {
                favoriteImg.setImageResource(R.drawable.ic_favorite);
            }
        }
        if (((MainActivity) context).bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mini_play_view.setAlpha(0);
            mini_play_view.setVisibility(View.INVISIBLE);
        } else if (((MainActivity) context).bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            player_layout.setAlpha(0);
            player_layout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public static void showToast(String option) {
        Toast.makeText(context, option, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            curPosTv.setText(convertDuration(String.valueOf(progress)));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //removing handler so we can change position of seekbar
        if (service_bound) {
            if (media_player_service.handler != null)
                media_player_service.handler.removeCallbacks(media_player_service.runnable);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //clearing the storage before putting new value
        new StorageUtil(getContext()).clearMusicLastPos();

        //storing the current position of seekbar in storage so we can access it from services
        new StorageUtil(getContext()).saveMusicLastPos(seekBar.getProgress());

        //first checking setting the media seek to current position of seek bar and then setting all data in UI
        if (service_bound) {
            if (media_player_service.handler != null)
                media_player_service.handler.removeCallbacks(media_player_service.runnable);
            if (media_player_service.media_player != null) {
                media_player_service.media_player.seekTo(seekBar.getProgress());
                if (media_player_service.media_player.isPlaying()) {
                    media_player_service.buildPlayNotification(PlaybackStatus.PLAYING, 1f);
                } else {
                    media_player_service.buildPausedNotification(PlaybackStatus.PAUSED, 0f);
                }
            }
            media_player_service.setSeekBar();
        } else {
            mini_progress.setProgress(seekBar.getProgress());
        }


    }

    public void setPreviousData() {
        MusicDataCapsule activeMusic = getMusic();

        if (activeMusic != null) {
            seekBarMain.setMax(Integer.parseInt(activeMusic.getsLength()));
            mini_progress.setMax(Integer.parseInt(activeMusic.getsLength()));
            durationTv.setText(convertDuration(activeMusic.getsLength()));

            int resumePosition = new StorageUtil(context).loadMusicLastPos();
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
        itemTouchHelper.startDrag(viewHolder);
    }
}