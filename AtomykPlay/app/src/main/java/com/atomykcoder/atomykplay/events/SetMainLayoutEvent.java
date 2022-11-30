package com.atomykcoder.atomykplay.events;

import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

public class SetMainLayoutEvent {

    public MusicDataCapsule activeMusic;

    public SetMainLayoutEvent(MusicDataCapsule activeMusic) {
        this.activeMusic = activeMusic;
    }
}
