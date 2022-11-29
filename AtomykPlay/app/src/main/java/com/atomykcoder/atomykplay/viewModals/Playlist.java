package com.atomykcoder.atomykplay.viewModals;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Playlist {
    private final String name;
//    private final String coverUri;
    private final Map<String, MusicDataCapsule> musicMap;

    public Playlist(String _name, Map<String, MusicDataCapsule> _musicMap) {
        name = _name;
        musicMap = _musicMap;
    }

    public Playlist(String _name) {
        name = _name;
        musicMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, MusicDataCapsule> getMusicMapList() {
        return musicMap;
    }

    public ArrayList<MusicDataCapsule> getMusicArrayList() {
        ArrayList<MusicDataCapsule> playlistItems = new ArrayList<>();
        for(Map.Entry<String, MusicDataCapsule> entry : musicMap.entrySet()) {
            playlistItems.add(entry.getValue());
        }
        return playlistItems;
    }

    public void addMusic(MusicDataCapsule music) {
        Log.i("info", "music added: " + music.getsId());
        musicMap.put(music.getsId(), music);
    }

    public void removeMusic(MusicDataCapsule music) {
        Log.i("info", "music removed");
        musicMap.remove(music.getsId());
    }


}
