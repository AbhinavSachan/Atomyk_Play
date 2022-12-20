package com.atomykcoder.atomykplay.events;


import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

public class RemoveFromFavoriteEvent {
    public MusicDataCapsule music;

    public RemoveFromFavoriteEvent(MusicDataCapsule _music) {
        music = _music;
    }
}
