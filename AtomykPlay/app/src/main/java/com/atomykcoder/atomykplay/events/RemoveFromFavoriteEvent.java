package com.atomykcoder.atomykplay.events;


public class RemoveFromFavoriteEvent {
    public String musicID;

    public RemoveFromFavoriteEvent(String _musicID) {
        musicID = _musicID;
    }
}
