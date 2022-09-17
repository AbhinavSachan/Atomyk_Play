package com.atomykcoder.atomykplay.function;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.MainActivity;
import com.atomykcoder.atomykplay.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class PlayerFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public static View miniPlayView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        miniPlayView = view.findViewById(R.id.mini_player_layout);





        return view;
    }
}