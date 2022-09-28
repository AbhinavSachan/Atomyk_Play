package com.atomykcoder.atomykplay.player;

import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PAUSE_PLAY_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PLAY_NEXT_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PLAY_PREVIOUS_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.is_granted;
import static com.atomykcoder.atomykplay.MainActivity.media_player_service;
import static com.atomykcoder.atomykplay.MainActivity.service_bound;
import static com.atomykcoder.atomykplay.MainActivity.service_connection;
import static com.atomykcoder.atomykplay.function.FetchMusic.convertDuration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.function.FetchMusic;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.atomykcoder.atomykplay.services.MediaPlayerService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;

@SuppressLint("StaticFieldLeak")
public class PlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    public static RelativeLayout mini_play_view;
    public static View player_layout;
    public static ImageView mini_cover, mini_pause, mini_next;
    public static LinearProgressIndicator mini_progress;
    public static TextView mini_name_text, mini_artist_text;
    //main player seekbar
    public static SeekBar seekBarMain;
    public static ImageView playImg;
    public static TextView curPosTv, durationTv;
    private static Context context;
    //cover image view
    private static ImageView playerCoverImage;
    private static View lyricsOpenLayout;
    private static ImageView queImg, repeatImg, previousImg, nextImg, shuffleImg, favoriteImg, timerImg, optionImg;
    private static TextView playerSongNameTv, playerArtistNameTv, mimeTv, bitrateTv;
    private int resumePosition = -1;
    //setting up mini player layout
    //calling it from service when player is prepared and also calling it in this fragment class
    //to set it on app start ☺

    public static void setMiniLayout() {

        StorageUtil storageUtil = new StorageUtil(context);
        ArrayList<MusicDataCapsule> musicList = storageUtil.loadMusic();
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
        ArrayList<MusicDataCapsule> musicList = storageUtil.loadMusic();
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
                Glide.with(context).load(getEmbeddedImage(activeMusic.getsPath())).apply(new RequestOptions().placeholder(R.drawable.ic_music_thumbnail))
                        .override(500, 500)
                        .into(playerCoverImage);

                try {
                    durationTv.setText(convertDuration(activeMusic.getsLength()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    playerSongNameTv.setText(activeMusic.getsName());
                    playerArtistNameTv.setText(activeMusic.getsArtist());
                    mimeTv.setText(getMime(activeMusic.getsMimeType()).toUpperCase());

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        try {
            context = getContext();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

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
        queImg = view.findViewById(R.id.player_que_iv);//○
        repeatImg = view.findViewById(R.id.player_repeat_iv);//○
        previousImg = view.findViewById(R.id.player_previous_iv);//○
        playImg = view.findViewById(R.id.player_play_iv);//○
        nextImg = view.findViewById(R.id.player_next_iv);//○
        shuffleImg = view.findViewById(R.id.player_shuffle_iv);//○
        favoriteImg = view.findViewById(R.id.player_favorite_iv);//○
        timerImg = view.findViewById(R.id.player_timer_iv);//○
        optionImg = view.findViewById(R.id.player_option_iv);//○
        playerSongNameTv = view.findViewById(R.id.player_song_name_tv);//○
        playerArtistNameTv = view.findViewById(R.id.player_song_artist_name_tv);//○
        bitrateTv = view.findViewById(R.id.player_bitrate_tv);//○
        mimeTv = view.findViewById(R.id.player_mime_tv);//○
        durationTv = view.findViewById(R.id.player_duration_tv);//○
        curPosTv = view.findViewById(R.id.player_current_pos_tv);//○
        lyricsOpenLayout = view.findViewById(R.id.player_lyrics_ll);

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
        lyricsOpenLayout.setOnClickListener(v-> openLyricsPanel());
        //top right option button
        optionImg.setOnClickListener(v -> optionMenu());

        seekBarMain.setOnSeekBarChangeListener(this);

        //layout setup ☺
        if (is_granted) {
            try {
                setMiniLayout();
                setMainPlayerLayout();
                setPreviousData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return view;
    }

    private void openLyricsPanel() {
        //show lyrics in bottom sheet
        showToast("lyrics");
    }

    private void optionMenu() {
        //add a bottom sheet to show music options like set to ringtone ,audio details ,add to favorite ,add to playlist etc.
        showToast("option");
    }

    private void setTimer() {
        //set a timer to set player turn off
        showToast("timer");
    }

    private void addFavorite() {
        //add to favorite and save it in shared pref
        showToast("favorite");
    }

    private void shuffleList() {
        //shuffle list program
        showToast("shuffle");
    }

    private void repeatFun() {
        //function for music list and only one music repeat and save that state in sharedPreference
        showToast("repeat");
    }

    private void openQue() {
        //show now playing music list
        showToast("song list");
    }

    public void pausePlayAudio() {
        if (!service_bound) {
            Intent playerIntent = new Intent(getContext(), MediaPlayerService.class);
            context.startService(playerIntent);
            context.bindService(playerIntent, service_connection, Context.BIND_AUTO_CREATE);
            service_bound = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //service is active send media with broadcast receiver
                    Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
                    context.sendBroadcast(broadcastIntent);
                }
            }, 20);
        } else {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
            context.sendBroadcast(broadcastIntent);
        }

    }

    public void playNextAudio() {
        if (!service_bound) {
            Intent playerIntent = new Intent(getContext(), MediaPlayerService.class);
            context.startService(playerIntent);
            context.bindService(playerIntent, service_connection, Context.BIND_AUTO_CREATE);
            service_bound = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //service is active send media with broadcast receiver
                    Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEXT_MUSIC);
                    context.sendBroadcast(broadcastIntent);
                }
            }, 20);
        } else {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEXT_MUSIC);
            context.sendBroadcast(broadcastIntent);
        }
    }

    public void playPreviousAudio() {
        if (!service_bound) {
            Intent playerIntent = new Intent(getContext(), MediaPlayerService.class);
            context.startService(playerIntent);
            context.bindService(playerIntent, service_connection, Context.BIND_AUTO_CREATE);
            service_bound = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //service is active send media with broadcast receiver
                    Intent broadcastIntent = new Intent(BROADCAST_PLAY_PREVIOUS_MUSIC);
                    context.sendBroadcast(broadcastIntent);
                }
            }, 20);

        } else {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_PREVIOUS_MUSIC);
            context.sendBroadcast(broadcastIntent);
        }
    }

    private void showToast(String option) {
        Toast.makeText(context, option, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //this will change time and audio in realtime
        if (service_bound) {
            if (fromUser) {
                media_player_service.media_player.seekTo(progress);
                curPosTv.setText(convertDuration(String.valueOf(progress)));
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //removing handler so we can change position of seekbar
        if (service_bound) {
            media_player_service.handler.removeCallbacks(media_player_service.runnable);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //clearing the storage before putting new value
        new StorageUtil(getContext()).clearCacheMusicLastPos();

        //storing the current position of seekbar in storage so we can access it from services
        new StorageUtil(getContext()).saveMusicLastPos(seekBar.getProgress());

        //first checking setting the media seek to current position of seek bar and then setting all data in UI
        if (service_bound) {
            media_player_service.handler.removeCallbacks(media_player_service.runnable);
            media_player_service.media_player.seekTo(seekBar.getProgress());
            media_player_service.setSeekBar();
        }

    }

    private void setPreviousData() {
        ArrayList<MusicDataCapsule> musicList;
        MusicDataCapsule activeMusic = null;
        int musicIndex;

        //load data from SharedPref
        StorageUtil storage = new StorageUtil(getContext());
        musicList = storage.loadMusic();
        musicIndex = storage.loadMusicIndex();

        if (musicList != null)
            if (musicIndex != -1 && musicIndex < musicList.size()) {
                activeMusic = musicList.get(musicIndex);
            }else {
                activeMusic = musicList.get(0);
            }

        if (activeMusic != null) {
            seekBarMain.setMax(Integer.parseInt(activeMusic.getsLength()));
            mini_progress.setMax(Integer.parseInt(activeMusic.getsLength()));
            durationTv.setText(convertDuration(activeMusic.getsLength()));

            resumePosition = new StorageUtil(getContext()).loadMusicLastPos();
            if (resumePosition != -1) {
                seekBarMain.setProgress(resumePosition);
                mini_progress.setProgress(resumePosition);
                String cur = convertDuration(String.valueOf(resumePosition));
                curPosTv.setText(cur);
            }

        }


    }

}