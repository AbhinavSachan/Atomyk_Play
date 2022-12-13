package com.atomykcoder.atomykplay.viewModals;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Playlist implements Serializable {
    private final String name;
    private final String coverUri;
    private final ArrayList<String> musicIDList;

    public Playlist(String _name, String _coverUri , ArrayList<String> _musicIDList) {
        name = _name;
        coverUri = _coverUri;
        musicIDList = _musicIDList;
    }

    public Playlist(String _name) {
        name = _name;
        coverUri = null;
        musicIDList = new ArrayList<>();
    }

    public Playlist(String _name, String _coverUri) {
        name = _name;
        coverUri = _coverUri;
        musicIDList = new ArrayList<>();
    }

    public String getCoverUri() {
        return coverUri;
    }

    public String getName() {
        return name;
    }


    /**
     * get music list in arraylist format with no keys
     * @return returns arraylist<MusicDataCapsule>
     */
    public ArrayList<String> getMusicIDList() {
        return musicIDList;
    }

    /**
     * add music in playlist
     * @param music music to be added
     */
    public void addMusic(String musicID) {
       musicIDList.add(musicID);
    }

    /**
     * remove music from playlist
     * @param music music to be removed
     */
    public void removeMusic(String musicID) {
        musicIDList.remove(musicID);
    }

    /**
     * clear all songs from playlist
     */
    public void clearPlaylist() {
        musicIDList.clear();
    }


}
