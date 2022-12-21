package com.atomykcoder.atomykplay.events;


import com.atomykcoder.atomykplay.data.Music;

public class RemoveFromFavoriteEvent {
    public Music music;

    public RemoveFromFavoriteEvent(Music _music) {
        music = _music;
    }
}
