package com.atomykcoder.atomykplay.events;


import com.atomykcoder.atomykplay.data.Music;

public class RemoveFromPlaylistEvent {
    public Music music;

    public RemoveFromPlaylistEvent(Music _music) {
        music = _music;
    }
}
