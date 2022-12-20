package com.atomykcoder.atomykplay.events;


import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

public class RemoveFromPlaylistEvent {
    public MusicDataCapsule music;

    public RemoveFromPlaylistEvent(MusicDataCapsule _music) {
        music = _music;
    }
}
