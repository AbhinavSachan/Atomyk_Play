package com.atomykcoder.atomykplay.function;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.MainActivity;
import com.atomykcoder.atomykplay.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class PlayerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) getActivity();
        View view;
        assert mainActivity != null;
        if (mainActivity.slidingUpPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.COLLAPSED) {
             view = inflater.inflate(R.layout.fragment_player, container, false);
        } else {
             view = inflater.inflate(R.layout.fragment_mini_player, container, false);
        }



        return view;
    }
}