package com.atomykcoder.atomykplay.events;

import com.atomykcoder.atomykplay.data.Music;

public class SetMainLayoutEvent {

    public Music activeMusic;

    public SetMainLayoutEvent(Music activeMusic) {
        this.activeMusic = activeMusic;
    }

}
