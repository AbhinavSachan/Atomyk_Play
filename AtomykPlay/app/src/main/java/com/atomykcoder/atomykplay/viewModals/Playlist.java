package com.atomykcoder.atomykplay.viewModals;

import java.io.Serializable;
import java.util.ArrayList;

public class Playlist implements Serializable {
    private final String name;
    private final String coverUri;
    private final ArrayList<MusicDataCapsule> musicList;

    public Playlist(String _name, String _coverUri , ArrayList<MusicDataCapsule> _musicList) {
        name = _name;
        coverUri = _coverUri;
        musicList = _musicList;
    }

    public Playlist(String _name) {
        name = _name;
        coverUri = null;
        musicList = new ArrayList<>();
    }

    public Playlist(String _name, String _coverUri) {
        name = _name;
        coverUri = _coverUri;
        musicList = new ArrayList<>();
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
    public ArrayList<MusicDataCapsule> getMusicList() {
        return musicList;
    }

    /**
     * add music in playlist
     * @param music music to be added
     */
    public void addMusic(MusicDataCapsule music) {
       musicList.add(music);
    }

    /**
     * remove music from playlist
     * @param music music to be removed
     */
    public void removeMusic(MusicDataCapsule music) {
        musicList.remove(music);
    }

    /**
     * clear all songs from playlist
     */
    public void clearPlaylist() {
        musicList.clear();
    }


}
