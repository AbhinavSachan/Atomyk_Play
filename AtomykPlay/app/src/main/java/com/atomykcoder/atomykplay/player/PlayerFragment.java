package com.atomykcoder.atomykplay.player;

import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PAUSE_PLAY_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PLAY_NEXT_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.BROADCAST_PLAY_PREVIOUS_MUSIC;
import static com.atomykcoder.atomykplay.MainActivity.media_player_service;
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
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class PlayerFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public static RelativeLayout mini_play_view;
    @SuppressLint("StaticFieldLeak")
    public static RelativeLayout player_layout;
    @SuppressLint("StaticFieldLeak")
    public static ImageView mini_cover, mini_pause, mini_next;
    public static LinearProgressIndicator mini_progress;
    @SuppressLint("StaticFieldLeak")
    public static TextView mini_name_text, mini_artist_text;
    private Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        try {
            context = getContext();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        player_layout = view.findViewById(R.id.player_layout);
        mini_play_view = view.findViewById(R.id.mini_player_layout);
        mini_cover = mini_play_view.findViewById(R.id.song_album_cover);
        mini_artist_text = mini_play_view.findViewById(R.id.song_artist_name);
        mini_name_text = mini_play_view.findViewById(R.id.song_name);
        mini_next = mini_play_view.findViewById(R.id.more_option_i_btn_next);
        mini_pause = mini_play_view.findViewById(R.id.more_option_i_btn_play);
        mini_progress = mini_play_view.findViewById(R.id.mini_player_progress);

        //click listeners on mini player
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



        return view;
    }


    public void pausePlayAudio() {
        if (service_bound) {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
            context.sendBroadcast(broadcastIntent);
        }

    }

    public void playNextAudio() {
        if (service_bound) {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEXT_MUSIC);
            context.sendBroadcast(broadcastIntent);
        }
    }

    public void playPreviousAudio() {
        if (service_bound) {
            //service is active send media with broadcast receiver
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_PREVIOUS_MUSIC);
            context.sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}