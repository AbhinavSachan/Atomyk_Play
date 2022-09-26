package com.atomykcoder.atomykplay.player;

import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PAUSE_PLAY_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PLAY_NEXT_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PLAY_PREVIOUS_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.is_granted;
import static com.atomykcoder.atomykplay.MainActivity.service_bound;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;

public class PlayerFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public static RelativeLayout mini_play_view;
    @SuppressLint("StaticFieldLeak")
    public static View player_layout;
    @SuppressLint("StaticFieldLeak")
    public static ImageView mini_cover, mini_pause, mini_next;
    public static LinearProgressIndicator mini_progress;
    @SuppressLint("StaticFieldLeak")
    public static TextView mini_name_text, mini_artist_text;
    //main player seekbar
    public static SeekBar seekBarMain;
    public static ImageView playImg;
    private static Context context;
    //cover image view
    private static ImageView playerCoverImage;
    private static View lyricsOpenLayout;
    private static ImageView queImg, repeatImg, previousImg, nextImg, shuffleImg, favoriteImg, timerImg, optionImg;
    public static TextView playerSongNameTv, playerArtistNameTv, mimeTypeTv, bitrateTv, durationTv, curPosTv;

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
                Glide.with(context).load(activeMusic.getsAlbumUri()).apply(new RequestOptions().placeholder(R.drawable.ic_music))
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
                Glide.with(context).load(activeMusic.getsAlbumUri()).apply(new RequestOptions().placeholder(R.drawable.ic_music_thumbnail))
                        .override(500, 500)
                        .into(playerCoverImage);

                try {
                    durationTv.setText(activeMusic.getsLength());
                    playerSongNameTv.setText(activeMusic.getsName());
                    playerArtistNameTv.setText(activeMusic.getsArtist());
                    mimeTypeTv.setText(activeMusic.getsMimeType());
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
        mini_play_view = view.findViewById(R.id.mini_player_layout);
        mini_cover = view.findViewById(R.id.song_album_cover_mini);
        mini_artist_text = view.findViewById(R.id.song_artist_name_mini);
        mini_name_text = view.findViewById(R.id.song_name_mini);
        mini_next = view.findViewById(R.id.more_option_i_btn_next);
        mini_pause = view.findViewById(R.id.more_option_i_btn_play);
        mini_progress = view.findViewById(R.id.mini_player_progress);

        //Main player items initializations
        player_layout = view.findViewById(R.id.player_layout);
        playerCoverImage = view.findViewById(R.id.player_cover_iv);
        seekBarMain = view.findViewById(R.id.player_seek_bar);
        queImg = view.findViewById(R.id.player_que_iv);
        repeatImg = view.findViewById(R.id.player_repeat_iv);
        previousImg = view.findViewById(R.id.player_previous_iv);
        playImg = view.findViewById(R.id.player_play_iv);
        nextImg = view.findViewById(R.id.player_next_iv);
        shuffleImg = view.findViewById(R.id.player_shuffle_iv);
        favoriteImg = view.findViewById(R.id.player_favorite_iv);
        timerImg = view.findViewById(R.id.player_timer_iv);
        optionImg = view.findViewById(R.id.player_option_iv);
        playerSongNameTv = view.findViewById(R.id.player_song_name_tv);
        playerArtistNameTv = view.findViewById(R.id.player_song_artist_name_tv);
        mimeTypeTv = view.findViewById(R.id.player_mime_type_tv);
        bitrateTv = view.findViewById(R.id.player_bitrate_tv);
        durationTv = view.findViewById(R.id.player_duration_tv);
        curPosTv = view.findViewById(R.id.player_current_pos_tv);
        lyricsOpenLayout = view.findViewById(R.id.player_lyrics_ll);

        //click listeners on mini player
        //and sending broadcast on click
        mini_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlayAudio();
            }
        });

        mini_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextAudio();
            }
        });
        previousImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousAudio();
            }
        });
        nextImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextAudio();
            }
        });
        playImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlayAudio();
            }
        });

        //layout setup ☺
        if (is_granted) {
            setMiniLayout();
            setMainPlayerLayout();
        }
        return view;
    }

    public void pausePlayAudio() {
        if (service_bound) {
            //service is active send media with broadcast receiver ♦
            Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
            context.sendBroadcast(broadcastIntent);
        }

    }

    public void playNextAudio() {
        if (service_bound) {
            //service is active send media with broadcast receiver ♦
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEXT_MUSIC);
            context.sendBroadcast(broadcastIntent);
        }
    }

    public void playPreviousAudio() {
        if (service_bound) {
            //service is active send media with broadcast receiver ♦
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_PREVIOUS_MUSIC);
            context.sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}