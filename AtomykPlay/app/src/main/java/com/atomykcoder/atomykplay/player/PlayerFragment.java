package com.atomykcoder.atomykplay.player;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class PlayerFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public static RelativeLayout miniPlayView;
    public static RelativeLayout playerLayout;
    public static ImageView miniCover;
    public static LinearProgressIndicator miniProgress;
    public static ImageButton miniPause, miniNext;
    public static TextView miniNameText, miniArtistText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        playerLayout = view.findViewById(R.id.player_layout);
        miniPlayView = view.findViewById(R.id.mini_player_layout);
        miniCover = miniPlayView.findViewById(R.id.song_album_cover);
        miniArtistText = miniPlayView.findViewById(R.id.song_artist_name);
        miniNameText = miniPlayView.findViewById(R.id.song_name);
        miniNext = miniPlayView.findViewById(R.id.more_option_i_btn_next);
        miniPause = miniPlayView.findViewById(R.id.more_option_i_btn_play);
        miniProgress = miniPlayView.findViewById(R.id.mini_player_progress);


        return view;
    }
}