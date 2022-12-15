package com.atomykcoder.atomykplay.events;


public class RemoveFromPlaylistEvent {
    public String musicID;

    public RemoveFromPlaylistEvent(String _musicID) {
       musicID = _musicID;
    }
}
