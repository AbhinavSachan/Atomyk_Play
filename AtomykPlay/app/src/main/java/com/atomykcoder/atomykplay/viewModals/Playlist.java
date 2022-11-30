package com.atomykcoder.atomykplay.viewModals;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Playlist implements Serializable {
    private final String name;
    private final String coverUri;
    private final Map<String, MusicDataCapsule> musicMap;

    public Playlist(String _name, String _coverUri , Map<String, MusicDataCapsule> _musicMap) {
        name = _name;
        coverUri = _coverUri;
        musicMap = _musicMap;
    }

    public Playlist(String _name) {
        name = _name;
        coverUri = null;
        musicMap = new HashMap<>();
    }

    public Playlist(String _name, String _coverUri) {
        name = _name;
        coverUri = _coverUri;
        musicMap = new HashMap<>();
    }

    public String getCoverUri() {
        return coverUri;
    }

    public String getName() {
        return name;
    }
    
    public Map<String, MusicDataCapsule> getMusicMapList() {
        return musicMap;
    }

    /**
     * get music list in arraylist format with no keys
     * @return returns arraylist<MusicDataCapsule>
     */
    public ArrayList<MusicDataCapsule> getMusicArrayList() {
        ArrayList<MusicDataCapsule> playlistItems = new ArrayList<>();
        for(Map.Entry<String, MusicDataCapsule> entry : musicMap.entrySet()) {
            playlistItems.add(entry.getValue());
        }
        return playlistItems;
    }

    /**
     * add music in playlist
     * @param music music to be added
     */
    public void addMusic(MusicDataCapsule music) {
        Log.i("info", "music added: " + music.getsId());
        musicMap.put(music.getsId(), music);
    }

    /**
     * remove music from playlist
     * @param music music to be removed
     */
    public void removeMusic(MusicDataCapsule music) {
        Log.i("info", "music removed");
        musicMap.remove(music.getsId());
    }

    /**
     * clear all songs from playlist
     */
    public void clearPlaylist() {
        musicMap.clear();
    }


}
