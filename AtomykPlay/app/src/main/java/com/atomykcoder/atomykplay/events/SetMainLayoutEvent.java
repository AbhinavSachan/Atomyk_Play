package com.atomykcoder.atomykplay.events;

import android.graphics.Bitmap;

import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

public class SetMainLayoutEvent {

    public MusicDataCapsule activeMusic;
    public Bitmap image;

    public SetMainLayoutEvent(MusicDataCapsule activeMusic,Bitmap image) {
        this.activeMusic = activeMusic;
        this.image = image;
    }
}
