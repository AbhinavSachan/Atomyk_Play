package com.atomykcoder.atomykplay.events;

import android.graphics.Bitmap;

import com.atomykcoder.atomykplay.data.Music;

public class SetMainLayoutEvent {

    public Music activeMusic;
    public Bitmap image;

    public SetMainLayoutEvent(Music activeMusic, Bitmap image) {
        this.activeMusic = activeMusic;
        this.image = image;
    }

}
